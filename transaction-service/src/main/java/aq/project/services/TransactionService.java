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
import io.github.resilience4j.retry.annotation.Retry;
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

    @Retry(
            name = "init-transaction-retry",
            fallbackMethod = "initTransactionRetryFallback"
    )
    @RateLimiter(
            name = "init-transaction-rate-limiter",
            fallbackMethod = "initTransactionRateLimiterFallback"
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

        String exceptionClass =  exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during init [%s] transaction for wallet with id: [%s]. Exception message: %s",
                exceptionClass, initTransactionRequestDto.getPaymentType(), walletId, exceptionMessage);

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    private InitTransactionResponseDto initTransactionRetryFallback(
            InitTransactionRequestDto initTransactionRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof IllegalStateException)
            throw exception;

        String action = "init-transaction";

        UUID walletId = initTransactionRequestDto.getWalletId();

        String exceptionClass =  exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during init [%s] transaction for wallet with id: [%s]. Exception message: %s",
                exceptionClass, initTransactionRequestDto.getPaymentType(), walletId, exceptionMessage);

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

    @Retry(
            name = "confirm-transaction-retry",
            fallbackMethod = "confirmTransactionRetryFallback"
    )
    @RateLimiter(
            name = "confirm-transaction-rate-limiter",
            fallbackMethod = "confirmTransactionRateLimiterFallback"
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

        newTransaction.setId(UUID.randomUUID());
        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setDescription(null);
        newTransaction.setWalletAmount(walletTotalAmount);

        Transaction savedTransaction = transactionRepository.save(newTransaction);

        PaymentType paymentType = confirmTransactionRequestDto.getPaymentType();

        switch(paymentType) {
            case DEPOSIT, WITHDRAWAL ->  {
                log.debug(">>>>> Attempt to publish [{}] transaction event with transaction id: [{}] into [Kafka]",
                        paymentType, savedTransaction.getId());

                transactionEventKafkaProducer.publishTransactionEvent(savedTransaction);
            }

            case TRANSFER -> {
                BigDecimal targetWalletConversionRate = confirmTransactionRequestDto.getTargetWalletConversionRate();
                BigDecimal targetWalletConvertedAmount = transactionAmount.multiply(targetWalletConversionRate);
                BigDecimal targetWalletFee = feeService.evaluateFeeForAmount(targetWalletConvertedAmount);
                BigDecimal targetWalletTotalAmount = targetWalletFee.add(targetWalletConvertedAmount);

                TransferWalletRequestDto dto = new TransferWalletRequestDto()
                        .sourceWalletId(savedTransaction.getWalletId())
                        .targetWalletId(savedTransaction.getTargetWalletId())
                        .transferAmount(savedTransaction.getWalletAmount())
                        .totalAmount(targetWalletTotalAmount)
                        .currencyCode(savedTransaction.getCurrencyCode());

                String traceId = traceContext.getTraceId();
                String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());

                UUID transactionId = savedTransaction.getId();

                log.debug(">>>>> Attempt to send [TRANSFER] transaction with id: [{}] into [wallet-service]",
                        transactionId);

                walletApiClient.transferWallet(dto, traceId, authorization);

                log.debug(">>>>> Success attempt to send [TRANSFER] transaction with id: [{}] into [wallet-service]",
                        transactionId);

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

        String exceptionClass =  exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during confirm [%s] transaction for wallet with id: [%s]. Exception message: %s",
                exceptionClass, confirmTransactionRequestDto.getPaymentType(), walletId, exceptionMessage);

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    private UUID confirmTransactionRetryFallback(
            ConfirmTransactionRequestDto confirmTransactionRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof IllegalStateException)
            throw exception;

        String action = "confirm-transaction";

        UUID walletId = confirmTransactionRequestDto.getWalletId();

        String exceptionClass =  exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during confirm [%s] transaction for wallet with id: [%s]. Exception message: %s",
                exceptionClass, confirmTransactionRequestDto.getPaymentType(), walletId, exceptionMessage);

        fallback.handleCircuitBreakerFallback(action, message, exception);

        return null;
    }

    @Retry(
            name = "confirm-transaction-retry",
            fallbackMethod = "confirmTransactionRetryFallback"
    )
    public void confirmTransaction(
            UUID transactionId
    ) {
        Transaction transaction = getTransaction(transactionId);

        if(!transaction.isPending()) {
            String traceId = traceContext.getTraceId();

            log.warn("[{}]: Incorrect attempt to confirm [{}] transaction with id: [{}] in: [{}] status",
                    traceId, transaction.getPaymentType(), transaction.getId(), transaction.getTransactionStatus());
            return;
        }

        String traceId = traceContext.getTraceId();
        String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());

        PaymentType paymentType = transaction.getPaymentType();

        log.debug(">>>>> Start to handle [{}}] transaction with id: [{}]",
                paymentType, transactionId);

        log.debug(">>>>> Attempt to send [{}] transaction with id: [{}] into [wallet-service]",
                paymentType, transactionId);

        switch (paymentType) {
            case DEPOSIT -> {
                DepositWalletRequestDto dto = new DepositWalletRequestDto()
                        .walletId(transaction.getWalletId())
                        .amount(transaction.getWalletAmount())
                        .currencyCode(transaction.getCurrencyCode());

                walletApiClient.depositWallet(dto, traceId, authorization);
            }

            case WITHDRAWAL -> {
                WithdrawalWalletRequestDto dto = new WithdrawalWalletRequestDto()
                        .walletId(transaction.getWalletId())
                        .amount(transaction.getWalletAmount())
                        .currencyCode(transaction.getCurrencyCode());

                walletApiClient.withdrawalWallet(dto, traceId, authorization);
            }
        }

        transaction.completeTransaction();

        log.debug(">>>>> Success attempt to send [{}] transaction with id: [{}] into [wallet-service]",
                paymentType, transactionId);
    }

    private void confirmTransactionRetryFallback(
            UUID transactionId,
            Exception exception
    ) throws Exception {
        if(exception instanceof IllegalStateException)
            throw exception;

        String action = "confirm-transaction";

        Transaction transaction = getTransaction(transactionId);

//        Mark transaction as FAILED after limit of retry was achieved
        transaction.failTransaction();

//        Publish transaction event to Kafka topic about fail transaction
        transactionEventKafkaProducer.publishTransactionEvent(transaction);

        String exceptionClass = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during confirm [%s] transaction for wallet with id: [%s]. Exception message: %s",
                exceptionClass, transaction.getPaymentType(), transaction.getWalletId(), exceptionMessage);

        try {
            fallback.handleCircuitBreakerFallback(action, message, exception);
        } catch (Exception e) {
//            Suppressed to success update status of transaction entity as FAILED.
        }
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

        String exceptionClass =  exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during getting transaction status for transaction with id: [%s]. Exception message: %s",
                exceptionClass, transactionId, exceptionMessage);

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
        log.debug(">>>>> Attempt to [fail] transaction with id: [{}]", transactionId);

        Transaction transaction = getTransaction(transactionId);

        if(!transaction.isPending()) {
            String traceId = traceContext.getTraceId();

            PaymentType paymentType = transaction.getPaymentType();

            TransactionStatus transactionStatus = transaction.getTransactionStatus();

            log.warn("[{}]: Incorrect attempt to fail [{}] transaction with id: [{}] in: [{}] status",
                    traceId, paymentType, transactionId, transactionStatus);

            String description = String.format(
                    "Incorrect attempt to fail [%s] transaction with id: [%s] in: [%s] status",
                    paymentType, transactionId, transactionStatus);

            transaction.setDescription(description);

            return;
        }

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
        }

        transaction.failTransaction();

        log.debug(">>>>> Success handle of request to [fail] transaction with id: [{}]", transactionId);
    }

    private void failTransactionRateLimiterFallback(
            UUID transactionId,
            RequestNotPermitted exception
    ) {
        String action = "fail-transaction";

        String exceptionClass =  exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during fail transaction with id: [%s]. Exception message: %s",
                exceptionClass, transactionId, exceptionMessage);

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

        String exceptionClass =  exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during fail transaction with id: [%s]. Exception message: %s",
                exceptionClass, transactionId, exceptionMessage);

        fallback.handleCircuitBreakerFallback(action, message, exception);
    }

    private Transaction getTransaction(
            UUID transactionId
    ) {
        return transactionRepository.findByIdWithLock(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id: [%s] not found", transactionId)));
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

        String exceptionClass =  exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during getting list of transaction info. Exception message: %s",
                exceptionClass, exceptionMessage);

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }
}