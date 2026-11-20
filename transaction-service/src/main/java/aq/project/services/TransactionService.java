package aq.project.services;

import aq.project.dto.*;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.kafka.TransactionEventKafkaProducer;
import aq.project.mappers.TransactionMapper;
import aq.project.repositories.TransactionRepository;
import aq.project.util.SpecificationFactory;
import aq.project.utils.jwt.JwtUtil;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import aq.project.wallet_service.WalletApiClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
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

    private final TransactionEventKafkaProducer transactionEventKafkaProducer;

    private final Fallback fallback;

    @RateLimiter(
            name = "init-transaction-rate-limiter",
            fallbackMethod = "initTransactionRateLimiterFallback"
    )
    @CircuitBreaker(
            name = "init-transaction-circuitbreaker",
            fallbackMethod = "initTransactionCircuitBreakerFallback"
    )
    public InitTransactionResponseDto initTransaction(
            InitTransactionRequestDto initTransactionRequestDto
    ) {
        String traceId = traceContext.getTraceId();

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
        BigDecimal walletConversionRate = initTransactionRequestDto.getWalletConversionRate();
        BigDecimal convertedAmount = amount.multiply(walletConversionRate);
        BigDecimal fee = feeService.evaluateFeeForAmount(convertedAmount);
        BigDecimal totalAmount = convertedAmount.add(fee);
        BigDecimal walletBalance = pendingWalletInfo.getBalance();

        String currencyCode = initTransactionRequestDto.getCurrencyCode();

        if(isBalanceExceedAmount(walletBalance, totalAmount)) {
            return new InitTransactionResponseDto()
                    .walletId(pendingWalletInfo.getWalletId())
                    .amount(amount)
                    .conversionRate(walletConversionRate)
                    .fee(fee)
                    .totalAmount(totalAmount)
                    .available(true)
                    .description(null)
                    .paymentType(paymentType)
                    .currencyCode(currencyCode);
        } else {
            log.warn("[{}]: Success init [{}] transaction for wallet with id: [{}]. " +
                            "Warning: initialization of transaction is not available. " +
                            "Amount of operation is exceeded current balance of wallet",
                    traceId, paymentType, initTransactionRequestDto.getWalletId());

            String description = String.format(
                    "Process [%s] transaction is not allowed for wallet with id: [%s]. " +
                    "Total amount of operation: [%s] is exceeded current balance of wallet",
                    paymentType, pendingWalletInfo.getWalletId(), totalAmount);

            return new InitTransactionResponseDto()
                    .walletId(pendingWalletInfo.getWalletId())
                    .amount(amount)
                    .conversionRate(walletConversionRate)
                    .fee(fee)
                    .totalAmount(totalAmount)
                    .available(false)
                    .description(description)
                    .paymentType(paymentType)
                    .currencyCode(currencyCode);
        }
    }

    private InitTransactionResponseDto initTransactionRateLimiterFallback(
            InitTransactionRequestDto initTransactionRequestDto,
            RequestNotPermitted exception
    ) {
        String action = "init-transaction";

        UUID walletId = initTransactionRequestDto.getWalletId();

        String message = String.format(
                "Exception occurred during init [%s] transaction for wallet with id: [%s]. Exception message: %s",
                initTransactionRequestDto.getPaymentType(), walletId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    private InitTransactionResponseDto initTransactionCircuitBreakerFallback(
            InitTransactionRequestDto initTransactionRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof IllegalStateException)
            throw exception;

        String action = "init-transaction";

        UUID walletId = initTransactionRequestDto.getWalletId();

        String message = String.format(
                "Exception occurred during init [%s] transaction for wallet with id: [%s]. Exception message: %s",
                initTransactionRequestDto.getPaymentType(), walletId, exception.getMessage());

        fallback.handleCircuitBreakerFallback(action, message, exception);

        return null;
    }

    private WalletInfoResponseDto getWalletInfo(UUID walletId) {
        String traceId = traceContext.getTraceId();
        String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());

        return walletApiClient.getWalletInfo(walletId, traceId, authorization).getBody();
    }

    private InitTransactionResponseDto initOnBlockedWallet(
            UUID walletId,
            PaymentType paymentType
    ) {
        String traceId = traceContext.getTraceId();

        log.warn("[{}]: Success init [{}] transaction for wallet with id: [{}]. " +
                        "Warning: initialization of transaction is not available. Wallet is blocked",
                traceId, paymentType, walletId);

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

    @RateLimiter(
            name = "confirm-transaction-rate-limiter",
            fallbackMethod = "confirmTransactionRateLimiterFallback"
    )
    @CircuitBreaker(
            name = "confirm-transaction-circuitbreaker",
            fallbackMethod = "confirmTransactionCircuitBreakerFallback"
    )
    @Transactional
    public UUID confirmTransaction(
            ConfirmTransactionRequestDto confirmTransactionRequestDto
    ) {
        BigDecimal transactionAmount = confirmTransactionRequestDto.getAmount();

        BigDecimal walletConversionRate = confirmTransactionRequestDto.getWalletConversionRate();
        BigDecimal walletConvertedAmount = transactionAmount.multiply(walletConversionRate);
        BigDecimal walletFee = feeService.evaluateFeeForAmount(walletConvertedAmount);
        BigDecimal walletTotalAmount = walletFee.add(walletConvertedAmount);

        Transaction newTransaction = transactionMapper.toTransaction(confirmTransactionRequestDto);

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setDescription(null);
        newTransaction.setWalletAmount(walletTotalAmount);

        Transaction savedTransaction = transactionRepository.save(newTransaction);

        PaymentType paymentType = confirmTransactionRequestDto.getPaymentType();

        String traceId = traceContext.getTraceId();
        String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());

        switch(paymentType) {
            case DEPOSIT ->  {
                DepositWalletRequestDto dto = new DepositWalletRequestDto()
                        .walletId(savedTransaction.getWalletId())
                        .amount(savedTransaction.getWalletAmount())
                        .currencyCode(savedTransaction.getCurrencyCode());

                walletApiClient.depositWallet(dto, traceId, authorization);

                transactionEventKafkaProducer.sendDepositTransactionEvent(savedTransaction);
            }

            case WITHDRAWAL -> {
                WithdrawalWalletRequestDto dto = new WithdrawalWalletRequestDto()
                        .walletId(savedTransaction.getWalletId())
                        .amount(savedTransaction.getWalletAmount())
                        .currencyCode(savedTransaction.getCurrencyCode());

                walletApiClient.withdrawalWallet(dto, traceId, authorization);

                transactionEventKafkaProducer.sendWithdrawalTransactionEvent(savedTransaction);
            }

            case TRANSFER -> {
                BigDecimal targetWalletConversionRate = confirmTransactionRequestDto.getTargetWalletConversionRate();
                BigDecimal targetWalletConvertedAmount = transactionAmount.multiply(targetWalletConversionRate);
                BigDecimal targetWalletFee = feeService.evaluateFeeForAmount(targetWalletConvertedAmount);
                BigDecimal targetWalletTotalAmount = targetWalletFee.add(targetWalletConvertedAmount);

                TransferWalletRequestDto dto = new TransferWalletRequestDto()
                        .sourceWalletId(savedTransaction.getWalletId())
                        .targetWalletId(savedTransaction.getTargetWalletId())
                        .walletAmount(savedTransaction.getWalletAmount())
                        .targetWalletAmount(targetWalletTotalAmount)
                        .currencyCode(savedTransaction.getCurrencyCode());

                walletApiClient.transferWallet(dto, traceId, authorization);

                savedTransaction.setTargetWalletAmount(targetWalletTotalAmount);
                savedTransaction.completeTransaction();
            }

            default -> {
                throw new IllegalStateException("Unexpected payment type: " + paymentType);
            }
        }
        return savedTransaction.getId();
    }

    private UUID confirmTransactionRateLimiterFallback(
            ConfirmTransactionRequestDto confirmTransactionRequestDto,
            RequestNotPermitted exception
    ) {
        String action = "confirm-transaction";

        UUID walletId = confirmTransactionRequestDto.getWalletId();

        String message = String.format(
                "Exception occurred during confirm [%s] transaction for wallet with id: [%s]. Exception message: %s",
                confirmTransactionRequestDto.getPaymentType(), walletId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    private UUID confirmTransactionCircuitBreakerFallback(
            ConfirmTransactionRequestDto confirmTransactionRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof IllegalStateException)
            throw exception;

        String action = "confirm-transaction";

        UUID walletId = confirmTransactionRequestDto.getWalletId();

        String message = String.format(
                "Exception occurred during confirm [%s] transaction for wallet with id: [%s]. Exception message: %s",
                confirmTransactionRequestDto.getPaymentType(), walletId, exception.getMessage());

        fallback.handleCircuitBreakerFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "get-transaction-status-rate-limiter",
            fallbackMethod = "getTransactionStatusRateLimiterFallback"
    )
    public TransactionStatus getTransactionStatus(
            UUID transactionId
    ) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id: [%s] not found", transactionId)))
                .getTransactionStatus();
    }

    private TransactionStatus getTransactionStatusRateLimiterFallback(
            UUID transactionId,
            RequestNotPermitted exception
    ) {
        String action = "get-transaction-status";

        String message = String.format(
                "Exception occurred during getting transaction status for transaction with id: [%s]. Exception message: %s",
                transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "fail-transaction-rate-limiter",
            fallbackMethod = "failTransactionRateLimiterFallback"
    )
    @CircuitBreaker(
            name = "fail-transaction-circuitbreaker",
            fallbackMethod = "failTransactionCircuitBreakerFallback"
    )
    @Transactional
    public void failTransaction(
            UUID transactionId
    ) {
        Transaction transaction = getTransaction(transactionId);

        checkTransactionInPendingState(transaction);

        PaymentType paymentType = transaction.getPaymentType();

        String traceId = traceContext.getTraceId() == null ? TraceFactory.genTraceId() : traceContext.getTraceId();
        String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());

        switch (paymentType) {
            case DEPOSIT -> {
//                    Compensation on fail
                WithdrawalWalletRequestDto dto = new WithdrawalWalletRequestDto()
                        .walletId(transaction.getWalletId())
                        .amount(transaction.getWalletAmount())
                        .currencyCode(transaction.getCurrencyCode());

                walletApiClient.withdrawalWallet(dto, traceId, authorization);
            }

            case WITHDRAWAL ->  {
//                    Compensation on fail
                DepositWalletRequestDto dto = new DepositWalletRequestDto()
                        .walletId(transaction.getWalletId())
                        .amount(transaction.getWalletAmount())
                        .currencyCode(transaction.getCurrencyCode());

                walletApiClient.depositWallet(dto, traceId, authorization);
            }

            case TRANSFER ->  {
//                    Compensation on fail
                TransferWalletRequestDto dto = new TransferWalletRequestDto()
                        .sourceWalletId(transaction.getTargetWalletId())
                        .targetWalletId(transaction.getWalletId())
                        .walletAmount(transaction.getTargetWalletAmount())
                        .targetWalletAmount(transaction.getWalletAmount())
                        .currencyCode(transaction.getCurrencyCode());

                walletApiClient.transferWallet(dto, traceId, authorization);
            }
        }
        transaction.failTransaction();
    }

    private void failTransactionRateLimiterFallback(
            UUID transactionId,
            RequestNotPermitted exception
    ) {
        String action = "fail-transaction";

        String message = String.format(
                "Exception occurred during fail transaction with id: [%s]. Exception message: %s",
                transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);
    }

    private void failTransactionCircuitBreakerFallback(
            UUID transactionId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException
                || exception instanceof IllegalStateException)
            throw exception;

        String action = "fail-transaction";

        String message = String.format(
                "Exception occurred during fail transaction with id: [%s]. Exception message: %s",
                transactionId, exception.getMessage());

        fallback.handleCircuitBreakerFallback(action, message, exception);
    }

    @RateLimiter(
            name = "complete-transaction-rate-limiter",
            fallbackMethod = "completeTransactionRateLimiterFallback"
    )
    @Transactional
    public void completeTransaction(
            UUID transactionId
    ) {
        Transaction transaction = getTransaction(transactionId);

        checkTransactionInPendingState(transaction);

        transaction.completeTransaction();
    }

    private void completeTransactionRateLimiterFallback(
            UUID transactionId,
            RequestNotPermitted exception
    ) {
        String action = "complete-transaction";

        String message = String.format(
                "Exception occurred during complete transaction with id: [%s]. Exception message: %s",
                transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);
    }

    private Transaction getTransaction(
            UUID transactionId
    ) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id: [%s] not found", transactionId)));
    }

    private void checkTransactionInPendingState(
            Transaction transaction
    ) {
        TransactionStatus transactionStatus = transaction.getTransactionStatus();

        PaymentType paymentType = transaction.getPaymentType();

        UUID transactionId = transaction.getId();

        String traceId = traceContext.getTraceId();

        if(transactionStatus != TransactionStatus.PENDING) {
            log.warn("[{}]: [{}] transaction with id: [{}] is not in [{}] state, current state: [{}]",
                    traceId, paymentType, transactionId, TransactionStatus.PENDING, transactionStatus);

            throw new IllegalStateException(
                    String.format("Unable to update transaction with id: [%s] in [%s] status",
                            transactionId, transactionStatus));
        }
    }

    @RateLimiter(
            name = "get-transactions-rate-limiter",
            fallbackMethod = "getTransactionsRateLimiterFallback"
    )
    public List<TransactionInfoResponseDto> getTransactions(
            UUID userId,
            UUID walletId,
            PaymentType type,
            TransactionStatus status,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Integer page,
            Integer size
    ) {
        final int defaultPage = page == null ? 0 : page;
        final int defaultSize = size == null ? 20 : size;

        PageRequest pageRequest = PageRequest.of(defaultPage, defaultSize);

        Specification<Transaction> spec = Specification
                .where(SpecificationFactory.getSpecFor("userId", userId))
                .and(SpecificationFactory.getSpecFor("walletId", walletId))
                .and(SpecificationFactory.getSpecFor("paymentType", type))
                .and(SpecificationFactory.getSpecFor("transactionStatus", status))
                .and(SpecificationFactory.getSpecGreaterOrEqualFor("createdAt", dateFrom))
                .and(SpecificationFactory.getSpecLessOrEqualFor("createdAt", dateTo));

        return transactionRepository.findAll(spec, pageRequest)
                .stream()
                .map(transactionMapper::toTransactionInfoResponseDto)
                .toList();
    }

    private List<TransactionInfoResponseDto> getTransactionsRateLimiterFallback(
            UUID userId,
            UUID walletId,
            PaymentType type,
            TransactionStatus status,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Integer page,
            Integer size,
            RequestNotPermitted exception
    ) {
        String action = "get-transactions";

        String message = String.format(
                "Exception occurred during getting list of transaction info. Exception message: %s",
                exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }
}