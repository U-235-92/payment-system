package aq.project.services;

import aq.project.dto.Operation;
import aq.project.dto.TransactionStatus;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
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
public class TransferTransactionService {

    @Value("${service.transaction-service.merchant-id}")
    private String merchantId;

    private final PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    private final TransactionServiceTransferTransactionRepository transactionServiceTransferTransactionRepository;
    private final PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    private final Fallback fallback;

    @Transactional
    @Bulkhead(
            name = "create-transfer-transaction-bulkhead",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "createTransactionBulkheadFallback"
    )
    @RateLimiter(
            name = "create-transfer-transaction-rate-limiter",
            fallbackMethod = "createTransactionRateLimiterFallback"
    )
    public UUID createTransaction(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction
    ) {
        transactionServiceTransferTransaction.getTransactionMetadata().setTimestamp(OffsetDateTime.now());
        transactionServiceTransferTransaction.setStatus(TransactionStatus.PENDING);

        TransactionServiceTransferTransaction savedTransactionServiceDepositTransaction = transactionServiceTransferTransactionRepository.save(transactionServiceTransferTransaction);

        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = paymentProviderTransactionDtoMapper.toPaymentProviderServiceTransactionRequest(savedTransactionServiceDepositTransaction);
        paymentProviderServiceTransactionRequest.setMerchantId(merchantId);
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);
        paymentProviderServiceTransactionRequest.setOperation(Operation.TRANSFER);

        paymentProviderServiceTransactionRequestRepository.save(paymentProviderServiceTransactionRequest);

        return savedTransactionServiceDepositTransaction.getId();
    }

    private UUID createTransactionBulkheadFallback(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            Exception exception
    ) {
        Operation operation = Operation.TRANSFER;

        String action = "create-transfer-transaction";
        String message = String.format(
                "Exception occurred during creating of [%s] transaction. Exception message: %s",
                operation, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);

        return null;
    }

    private UUID createTransactionRateLimiterFallback(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            Exception exception
    ) {
        Operation operation = Operation.TRANSFER;

        String action = "create-transfer-transaction";
        String message = String.format(
                "Exception occurred during creating of [%s] transaction. Exception message: %s",
                operation, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @Bulkhead(
            name = "get-transfer-transaction-status-bulkhead",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "getTransactionStatusBulkheadFallback"
    )
    @RateLimiter(
            name = "get-transfer-transaction-status-rate-limiter",
            fallbackMethod = "getTransactionStatusRateLimiterFallback"
    )
    public TransactionStatus getTransactionStatus(
            UUID transactionId
    ) {
        return transactionServiceTransferTransactionRepository.findById(transactionId)
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

        Operation operation = Operation.TRANSFER;

        String action = "get-transfer-transaction-status";
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

        Operation operation = Operation.TRANSFER;

        String action = "get-transfer-transaction-status";
        String message = String.format(
                "Exception occurred during getting of [%s] transaction status with transaction id: [%s]. " +
                "Exception message: %s",
                operation, transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }
}
