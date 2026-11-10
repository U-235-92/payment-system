package aq.project.services;

import aq.project.dto.*;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.kafka.KafkaEventProducer;
import aq.project.mappers.TransactionMapper;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.TraceContext;
import aq.project.wallet_service.WalletApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final TransactionRepository transactionRepository;

    private final WalletApiClient walletApiClient;

    private final TokenService tokenService;

    private final FeeService feeService;

    private final TraceContext traceContext;

    private final KafkaEventProducer kafkaEventProducer;

    private Fallback fallback;

    public InitTransactionResponseDto initTransaction(
            InitTransactionRequestDto initTransactionRequestDto
    ) {
        PaymentType paymentType = initTransactionRequestDto.getPaymentType();

        WalletInfoResponseDto pendingWalletInfo;

        switch (paymentType) {
            case DEPOSIT, WITHDRAWAL -> {
                UUID walletId = initTransactionRequestDto.getWalletId();

                WalletInfoResponseDto walletInfo = getWalletInfo(walletId);

                if(walletInfo.getWalletStatus() == WalletStatus.BLOCKED)
                    return initOnBlockedWallet(walletId, paymentType);

                pendingWalletInfo = walletInfo;
            }

            case TRANSFER -> {
                UUID senderWalletId = initTransactionRequestDto.getWalletId();
                UUID recipientWalletId = initTransactionRequestDto.getTargetWalletId();

                WalletInfoResponseDto sourceWalletInfo = getWalletInfo(senderWalletId);
                WalletInfoResponseDto targetWalletInfo = getWalletInfo(recipientWalletId);

                if(sourceWalletInfo.getWalletStatus() == WalletStatus.BLOCKED)
                    return initOnBlockedWallet(senderWalletId, paymentType);

                if(targetWalletInfo.getWalletStatus() == WalletStatus.BLOCKED)
                    return initOnBlockedWallet(recipientWalletId, paymentType);

                pendingWalletInfo = sourceWalletInfo;
            }

            default -> {
                throw new IllegalStateException("Unexpected payment type: " + paymentType);
            }
        }

        BigDecimal amount = initTransactionRequestDto.getAmount();
        BigDecimal conversionRate = initTransactionRequestDto.getConversionRate();
        BigDecimal convertedAmount = amount.multiply(conversionRate);
        BigDecimal fee = feeService.evaluateFeeForAmount(convertedAmount);
        BigDecimal totalAmount = convertedAmount.add(fee);
        BigDecimal walletBalance = pendingWalletInfo.getBalance();

        String currencyCode = initTransactionRequestDto.getCurrencyCode();

        if(isBalanceExceedAmount(walletBalance, totalAmount)) {
            return new InitTransactionResponseDto()
                    .walletId(pendingWalletInfo.getWalletId())
                    .amount(amount)
                    .conversionRate(conversionRate)
                    .fee(fee)
                    .totalAmount(totalAmount)
                    .available(true)
                    .description(null)
                    .paymentType(paymentType)
                    .currencyCode(currencyCode);
        } else {
            log.warn("Success init [{}] transaction for wallet with id: [{}]. " +
                            "Warning: initialization of transaction is not available. " +
                            "Amount of operation is exceeded current balance of wallet",
                    paymentType, initTransactionRequestDto.getWalletId());

            String description = String.format(
                    "Process [%s] transaction is not allowed for wallet with id: [%s]. " +
                    "Total amount of operation: [%s] is exceeded current balance of wallet",
                    paymentType, pendingWalletInfo.getWalletId(), totalAmount);

            return new InitTransactionResponseDto()
                    .walletId(pendingWalletInfo.getWalletId())
                    .amount(amount)
                    .conversionRate(conversionRate)
                    .fee(fee)
                    .totalAmount(totalAmount)
                    .available(false)
                    .description(description)
                    .paymentType(paymentType)
                    .currencyCode(currencyCode);
        }
    }

    private WalletInfoResponseDto getWalletInfo(UUID walletId) {
        String traceId = traceContext.getTraceId();
        String authorization = tokenService.getAdminJwtAsAuthorizationHeaderValue();

        return walletApiClient.getWalletInfo(walletId, traceId, authorization).getBody();
    }

    private InitTransactionResponseDto initOnBlockedWallet(
            UUID walletId,
            PaymentType paymentType
    ) {
        log.warn("Success init [{}] transaction for wallet with id: [{}]. " +
                        "Warning: initialization of transaction is not available. Wallet is blocked",
                paymentType, walletId);

        String description = String.format("Process [%s] transaction is not allowed for wallet with id: [%s]. " +
                "Wallet is blocked", paymentType, walletId);

        return new InitTransactionResponseDto()
                .walletId(walletId)
                .description(description)
                .available(false)
                .paymentType(paymentType);
    }

    private boolean isBalanceExceedAmount(
            BigDecimal balance,
            BigDecimal amount
    ) {
        return amount != null
                && balance != null
                && balance.compareTo(amount) >= 0;
    }

    @Transactional
    public UUID confirmTransaction(
            ConfirmTransactionRequestDto confirmTransactionRequestDto
    ) {
        Transaction newTransaction = transactionMapper.toTransaction(confirmTransactionRequestDto);

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setDescription(null);
        newTransaction.setFee(feeService.evaluateFeeForAmount(newTransaction.getAmount()));

        Transaction savedTransaction = transactionRepository.save(newTransaction);

        PaymentType paymentType = confirmTransactionRequestDto.getPaymentType();

        String traceId = traceContext.getTraceId();
        String authorization = tokenService.getAdminJwtAsAuthorizationHeaderValue();

        switch(paymentType) {
            case DEPOSIT ->  {
                DepositWalletRequestDto dto = new DepositWalletRequestDto()
                        .walletId(savedTransaction.getWalletId())
                        .amount(savedTransaction.getAmount())
                        .currencyCode(savedTransaction.getCurrencyCode());

                walletApiClient.depositWallet(traceId, dto, authorization);

                kafkaEventProducer.sendDepositTransactionRequestEvent(savedTransaction);
            }

            case WITHDRAWAL -> {
                WithdrawalWalletRequestDto dto = new WithdrawalWalletRequestDto()
                        .walletId(savedTransaction.getWalletId())
                        .amount(savedTransaction.getAmount())
                        .currencyCode(savedTransaction.getCurrencyCode());

                walletApiClient.withdrawalWallet(traceId, dto, authorization);

                kafkaEventProducer.sendWithdrawalTransactionRequestEvent(savedTransaction);
            }

            case TRANSFER -> {
                TransferWalletRequestDto dto = new TransferWalletRequestDto()
                        .sourceWalletId(savedTransaction.getWalletId())
                        .targetWalletId(savedTransaction.getTargetWalletId())
                        .amount(savedTransaction.getAmount())
                        .currencyCode(savedTransaction.getCurrencyCode());

                walletApiClient.transferWallet(traceId, dto, authorization);

                savedTransaction.completeTransaction();
            }

            default -> {
                throw new IllegalStateException("Unexpected payment type: " + paymentType);
            }
        }
        return savedTransaction.getId();
    }

    public TransactionStatus getTransactionStatus(
            UUID transactionId
    ) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id: [%s] not found", transactionId)))
                .getTransactionStatus();
    }
}