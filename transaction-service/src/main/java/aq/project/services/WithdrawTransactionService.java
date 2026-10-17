package aq.project.services;

import aq.project.dto.Operation;
import aq.project.dto.TransactionStatus;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceWithdrawTransactionRequestRepository;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.mappers.WalletServiceTransactionDtoMapper;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawTransactionService {

    @Value("${service.transaction-service.merchant-id}")
    private String merchantId;

    private final PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;
    private final WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;

    private final TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    private final PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;
    private final WalletServiceWithdrawTransactionRequestRepository walletServiceWithdrawTransactionRequestRepository;

    private final Fallback fallback;

    @Transactional
    @Bulkhead(
            name = "create-withdraw-transaction-bulkhead",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "createTransactionBulkheadFallback"
    )
    @RateLimiter(
            name = "create-withdraw-transaction-rate-limiter",
            fallbackMethod = "createTransactionRateLimiterFallback"
    )
    public UUID createTransaction(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction
    ) {
        transactionServiceWithdrawTransaction.getTransactionMetadata().setTimestamp(OffsetDateTime.now());
        transactionServiceWithdrawTransaction.setStatus(TransactionStatus.PENDING);

        TransactionServiceWithdrawTransaction savedTransactionServiceDepositTransaction = transactionServiceWithdrawTransactionRepository.save(transactionServiceWithdrawTransaction);

        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = paymentProviderTransactionDtoMapper.toPaymentProviderServiceTransactionRequest(savedTransactionServiceDepositTransaction);
        paymentProviderServiceTransactionRequest.setMerchantId(merchantId);
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);
        paymentProviderServiceTransactionRequest.setOperation(Operation.WITHDRAW);

        paymentProviderServiceTransactionRequestRepository.save(paymentProviderServiceTransactionRequest);

        return savedTransactionServiceDepositTransaction.getId();
    }

    private UUID createTransactionBulkheadFallback(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            Exception exception
    ) {
        Operation operation = Operation.WITHDRAW;

        String action = "create-withdraw-transaction";
        String message = String.format(
                "Exception occurred during creating of [%s] transaction. Exception message: %s",
                operation, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);

        return null;
    }

    private UUID createTransactionRateLimiterFallback(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            Exception exception
    ) {
        Operation operation = Operation.WITHDRAW;

        String action = "create-withdraw-transaction";
        String message = String.format(
                "Exception occurred during creating of [%s] transaction. Exception message: %s",
                operation, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @Bulkhead(
            name = "get-withdraw-transaction-status-bulkhead",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "getTransactionStatusBulkheadFallback"
    )
    @RateLimiter(
            name = "get-withdraw-transaction-status-rate-limiter",
            fallbackMethod = "getTransactionStatusRateLimiterFallback"
    )
    public TransactionStatus getTransactionStatus(
            UUID transactionId
    ) {
        return transactionServiceWithdrawTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No transaction found with id: [%s]", transactionId)))
                .getStatus();
    }

    private TransactionStatus getTransactionStatusBulkheadFallback(
            UUID transactionId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        Operation operation = Operation.WITHDRAW;

        String action = "get-withdraw-transaction-status";
        String message = String.format(
                "Exception occurred during getting of [%s] transaction status with transaction id: [%s]. " +
                "Exception message: %s",
                operation, transactionId, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);

        return null;
    }

    private TransactionStatus getTransactionStatusRateLimiterFallback(
            UUID transactionId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        Operation operation = Operation.WITHDRAW;

        String action = "get-withdraw-transaction-status";
        String message = String.format(
                "Exception occurred during getting of [%s] transaction status with transaction id: [%s]. " +
                "Exception message: %s",
                operation, transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @Bulkhead(
            name = "fail-withdraw-transaction-bulkhead",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "failTransactionBulkheadFallback"
    )
    @RateLimiter(
            name = "fail-withdraw-transaction-rate-limiter",
            fallbackMethod = "failTransactionRateLimiterFallback"
    )
    @Transactional
    public void failTransaction(
            UUID transactionId
    ) {
        TransactionServiceWithdrawTransaction transaction = transactionServiceWithdrawTransactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id: [%s] not found", transactionId)));

        TransactionStatus currentTransactionStatus = transaction.getStatus();
        TransactionStatus targetTransactionStatus = TransactionStatus.FAILED;

        Operation operation = Operation.WITHDRAW;

        switch (currentTransactionStatus) {
            case PENDING, COMPLETED -> transaction.setStatus(targetTransactionStatus);

            default -> throw new ProcessOperationException(
                    String.format(
                            "Attempt to change status of: [%s] transaction with id: [%s] " +
                            "from: [%s] to: [%s] which is prohibited",
                            operation, transactionId, currentTransactionStatus, targetTransactionStatus));
        }

        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = paymentProviderTransactionDtoMapper
                .toPaymentProviderServiceTransactionRequest(transaction);
        paymentProviderServiceTransactionRequest.setMerchantId(merchantId);
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);
        paymentProviderServiceTransactionRequest.setOperation(operation);
        paymentProviderServiceTransactionRequest.setStatus(targetTransactionStatus);

        WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest = walletServiceTransactionDtoMapper
                .toWalletServiceWithdrawTransactionRequest(transaction);
        walletServiceWithdrawTransactionRequest.getTransactionRequestMetadata().setProcessed(false);
        walletServiceWithdrawTransactionRequest.setStatus(targetTransactionStatus);

        paymentProviderServiceTransactionRequestRepository.save(paymentProviderServiceTransactionRequest);
        walletServiceWithdrawTransactionRequestRepository.save(walletServiceWithdrawTransactionRequest);
    }

    public void failTransactionBulkheadFallback(
            UUID transactionId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException
                || exception instanceof ProcessOperationException)
            throw exception;

        Operation operation = Operation.WITHDRAW;

        String action = "fail-transaction";
        String message = String.format(
                "Exception occurred during fail of [%s] transaction with transaction id: [%s]. " +
                "Exception message: %s",
                operation, transactionId, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);
    }

    public void failTransactionRateLimiterFallback(
            UUID transactionId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException
                || exception instanceof ProcessOperationException)
            throw exception;

        Operation operation = Operation.WITHDRAW;

        String action = "fail-transaction";
        String message = String.format(
                "Exception occurred during fail of [%s] transaction with transaction id: [%s]. " +
                "Exception message: %s",
                operation, transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);
    }
}
