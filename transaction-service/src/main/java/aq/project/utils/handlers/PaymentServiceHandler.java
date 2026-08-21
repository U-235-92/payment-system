package aq.project.utils.handlers;

import aq.project.dto.CancelTransactionDto;
import aq.project.dto.TransactionRequestDto;
import aq.project.messages.TransactionRequest;
import aq.project.payment_provider_service.TransactionApiClient;
import aq.project.utils.mappers.TransactionRequestMapper;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.resilence.FallbackExceptionPredicate;
import aq.project.utils.security.BasicAuthorizationHeaderGenerator;
import aq.project.utils.telemetry.TraceContext;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static aq.project.utils.constants.RequestPropertyKeys.TRANSACTION_ID;
import static aq.project.utils.constants.RequestPropertyKeys.TRANSACTION_STATUS;

@Component
@RequiredArgsConstructor
public class PaymentServiceHandler {

    @Value("${service.transaction-service.merchant-id}")
    private String merchantId;
    @Value("${service.transaction-service.merchant-secret}")
    private String merchantSecret;

    private final FallbackExceptionPredicate<Exception> fallbackExceptionPredicate = new FallbackExceptionPredicate<Exception>();

    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;

    private final TransactionApiClient paymentServiceTransactionApiClient;

    private final TraceContext traceContext;

    private final Fallback fallback;

    @Retry(
            name = "send-transaction-request-to-payment-service",
            fallbackMethod = "sendTransactionRequestRetryFallback"
    )
    @Bulkhead(
            name = "send-transaction-request-to-payment-service",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "sendTransactionRequestBulkheadFallback"
    )
    @RateLimiter(
            name = "send-transaction-request-to-payment-service",
            fallbackMethod = "sendTransactionRequestRateLimiterFallback"
    )
    @CircuitBreaker(
            name = "send-transaction-request-to-payment-service",
            fallbackMethod = "sendTransactionRequestCircuitBreakerFallback"
    )
    public void sendTransactionRequestToPaymentService(TransactionRequest transactionRequest) {
        TransactionRequestDto requestDto = transactionRequestMapper.toTransactionRequestDto(transactionRequest);
        requestDto.getProperties().put(TRANSACTION_ID, transactionRequest.getTransactionId());
        requestDto.getProperties().put(TRANSACTION_STATUS, transactionRequest.getTransactionStatus().getValue());

        String xTraceId = traceContext.getTraceId();

        String basicAuthorizationHeaderValue = BasicAuthorizationHeaderGenerator
                .getBase64BasicAuthorizationValue(merchantId, merchantSecret);

        paymentServiceTransactionApiClient
                .createTransaction(basicAuthorizationHeaderValue, requestDto, xTraceId);
    }

    private void sendTransactionRequestRetryFallback(TransactionRequest transactionRequest, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String transactionId = getTransactionId(transactionRequest);
        String operationType = transactionRequest.getOperationType().getValue().toLowerCase();
        String action = "send-transaction-request";
        String message = String.format("Error occurred during sending [%s] transaction request with id [%s]",
                operationType, transactionId);

        fallback.handleRetryFallback(action, message, e);
    }

    private void sendTransactionRequestBulkheadFallback(TransactionRequest transactionRequest, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String transactionId = getTransactionId(transactionRequest);
        String operationType = transactionRequest.getOperationType().getValue().toLowerCase();
        String action = "send-transaction-request";
        String message = String.format("Error occurred during sending [%s] transaction request with id [%s]",
                operationType, transactionId);

        fallback.handleBulkheadFallback(action, message, e);
    }

    private void sendTransactionRequestRateLimiterFallback(TransactionRequest transactionRequest, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String transactionId = getTransactionId(transactionRequest);
        String operationType = transactionRequest.getOperationType().getValue().toLowerCase();
        String action = "send-transaction-request";
        String message = String.format("Error occurred during sending [%s] transaction request with id [%s]",
                operationType, transactionId);

        fallback.handleRateLimiterFallback(action, message, e);
    }

    private void sendTransactionRequestCircuitBreakerFallback(TransactionRequest transactionRequest, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String transactionId = getTransactionId(transactionRequest);
        String operationType = transactionRequest.getOperationType().getValue().toLowerCase();
        String action = "send-transaction-request";
        String message = String.format("Error occurred during sending [%s] transaction request with id [%s]",
                operationType, transactionId);

        fallback.handleCircuitBreakerFallback(action, message, e);
    }

    private String getTransactionId(TransactionRequest transactionRequest) {
        return transactionRequest.getTransactionId() == null
                ? ""
                : transactionRequest.getTransactionId();
    }

    @Retry(
            name = "send-cancel-transaction-request-to-payment-service",
            fallbackMethod = "sendCancelTransactionRequestRetryFallback"
    )
    @Bulkhead(
            name = "send-cancel-transaction-request-to-payment-service",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "sendCancelTransactionRequestBulkheadFallback"
    )
    @RateLimiter(
            name = "send-cancel-transaction-request-to-payment-service",
            fallbackMethod = "sendCancelTransactionRequestRateLimiterFallback"
    )
    @CircuitBreaker(
            name = "send-cancel-transaction-request-to-payment-service",
            fallbackMethod = "sendCancelTransactionRequestCircuitBreakerFallback"
    )
    public void sendCancelTransactionRequestToPaymentService(CancelTransactionDto cancelTransactionDto) {
        String xTraceId = traceContext.getTraceId();

        String basicAuthorizationHeaderValue = BasicAuthorizationHeaderGenerator
                .getBase64BasicAuthorizationValue(merchantId, merchantSecret);

        paymentServiceTransactionApiClient
                .cancelTransaction(basicAuthorizationHeaderValue, cancelTransactionDto, xTraceId);
    }

    private void sendCancelTransactionRequestRetryFallback(CancelTransactionDto cancelTransactionDto, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String transactionId = cancelTransactionDto.getId();
        String action = "send-cancel-transaction-request";
        String message = String.format("Error occurred during sending cancel transaction request with id [%s]",
                transactionId);

        fallback.handleRetryFallback(action, message, e);
    }

    private void sendCancelTransactionRequestBulkheadFallback(CancelTransactionDto cancelTransactionDto, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String transactionId = cancelTransactionDto.getId();
        String action = "send-cancel-transaction-request";
        String message = String.format("Error occurred during sending cancel transaction request with id [%s]",
                transactionId);

        fallback.handleBulkheadFallback(action, message, e);
    }

    private void sendCancelTransactionRequestRateLimiterFallback(CancelTransactionDto cancelTransactionDto, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String transactionId = cancelTransactionDto.getId();
        String action = "send-cancel-transaction-request";
        String message = String.format("Error occurred during sending cancel transaction request with id [%s]",
                transactionId);

        fallback.handleRateLimiterFallback(action, message, e);
    }

    private void sendCancelTransactionRequestCircuitBreakerFallback(CancelTransactionDto cancelTransactionDto, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String transactionId = cancelTransactionDto.getId();
        String action = "send-cancel-transaction-request";
        String message = String.format("Error occurred during sending cancel transaction request with id [%s]",
                transactionId);

        fallback.handleCircuitBreakerFallback(action, message, e);
    }
}
