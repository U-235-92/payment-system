package aq.project.services;

import aq.project.dto.Operation;
import aq.project.dto.TransactionStatus;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.mappers.WalletServiceTransactionDtoMapper;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositTransactionService {

    @Value("${service.transaction-service.merchant-id}")
    private String merchantId;

    private final PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;
    private final WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;

    private final TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    private final PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;
    private final WalletServiceDepositTransactionRequestRepository walletServiceDepositTransactionRequestRepository;

    private final Fallback fallback;

    @Bulkhead(
            name = "create-deposit-transaction-bulkhead",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "createTransactionBulkheadFallback"
    )
    @RateLimiter(
            name = "create-deposit-transaction-rate-limiter",
            fallbackMethod = "createTransactionRateLimiterFallback"
    )
    @Transactional
    public UUID createTransaction(
            TransactionServiceDepositTransaction transactionServiceDepositTransaction
    ) {
        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository
                .save(transactionServiceDepositTransaction);

        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = paymentProviderTransactionDtoMapper
                .toPaymentProviderServiceTransactionRequest(savedTransactionServiceDepositTransaction);

        paymentProviderServiceTransactionRequest.setMerchantId(merchantId);
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);
        paymentProviderServiceTransactionRequest.setOperation(Operation.DEPOSIT);
        paymentProviderServiceTransactionRequest.setStatus(TransactionStatus.PENDING);

        paymentProviderServiceTransactionRequestRepository.save(paymentProviderServiceTransactionRequest);

        return savedTransactionServiceDepositTransaction.getId();
    }

    private UUID createTransactionBulkheadFallback(
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            Exception exception
    ) {
        Operation operation = Operation.DEPOSIT;

        String action = "create-deposit-transaction";
        String message = String.format(
                "Exception occurred during creating of [%s] transaction. Exception message: %s",
                operation, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);

        return null;
    }

    private UUID createTransactionRateLimiterFallback(
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            Exception exception
    ) {
        Operation operation = Operation.DEPOSIT;

        String action = "create-deposit-transaction";
        String message = String.format(
                "Exception occurred during creating of [%s] transaction. Exception message: %s",
                operation, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @Bulkhead(
            name = "get-deposit-transaction-status-bulkhead",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "getTransactionStatusBulkheadFallback"
    )
    @RateLimiter(
            name = "get-deposit-transaction-status-rate-limiter",
            fallbackMethod = "getTransactionStatusRateLimiterFallback"
    )
    public TransactionStatus getTransactionStatus(
            UUID transactionId
    ) {
        return transactionServiceDepositTransactionRepository.findById(transactionId)
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

        Operation operation = Operation.DEPOSIT;

        String action = "get-deposit-transaction-status";
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

        Operation operation = Operation.DEPOSIT;

        String action = "get-deposit-transaction-status";
        String message = String.format(
                "Exception occurred during getting of [%s] transaction status with transaction id: [%s]. " +
                "Exception message: %s",
                operation, transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @Bulkhead(
            name = "fail-deposit-transaction-bulkhead",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "failTransactionBulkheadFallback"
    )
    @RateLimiter(
            name = "fail-deposit-transaction-rate-limiter",
            fallbackMethod = "failTransactionRateLimiterFallback"
    )
    @Transactional
    public void failTransaction(
            UUID transactionId
    ) {
        TransactionServiceDepositTransaction transaction = transactionServiceDepositTransactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id: [%s] not found", transactionId)));

        TransactionStatus currentTransactionStatus = transaction.getStatus();
        TransactionStatus targetTransactionStatus = TransactionStatus.FAILED;

        Operation operation = Operation.DEPOSIT;

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

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = walletServiceTransactionDtoMapper
                .toWalletServiceDepositTransactionRequest(transaction);
        walletServiceDepositTransactionRequest.getTransactionRequestMetadata().setProcessed(false);
        walletServiceDepositTransactionRequest.setStatus(targetTransactionStatus);

        paymentProviderServiceTransactionRequestRepository.save(paymentProviderServiceTransactionRequest);
        walletServiceDepositTransactionRequestRepository.save(walletServiceDepositTransactionRequest);
    }

    public void failTransactionBulkheadFallback(
            UUID transactionId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException
                || exception instanceof ProcessOperationException)
            throw exception;

        Operation operation = Operation.DEPOSIT;

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

        Operation operation = Operation.DEPOSIT;

        String action = "fail-transaction";
        String message = String.format(
                "Exception occurred during fail of [%s] transaction with transaction id: [%s]. " +
                "Exception message: %s",
                operation, transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);
    }
}
