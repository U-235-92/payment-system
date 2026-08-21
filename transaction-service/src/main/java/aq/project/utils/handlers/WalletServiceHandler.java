package aq.project.utils.handlers;

import aq.project.dto.TransactionStatus;
import aq.project.services.TokenService;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.resilence.FallbackExceptionPredicate;
import aq.project.utils.telemetry.TraceContext;
import aq.project.wallet_service.TransactionApiClient;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletServiceHandler {

    private final FallbackExceptionPredicate<Exception> fallbackExceptionPredicate = new FallbackExceptionPredicate<Exception>();

    private final TokenService tokenService;

    private final TraceContext traceContext;

    private final TransactionApiClient transactionApiClient;

    private final Fallback fallback;

    @Retry(
            name = "get-transaction-status",
            fallbackMethod = "getTransactionStatusRetryFallback"
    )
    @Bulkhead(
            name = "get-transaction-status",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "getTransactionStatusBulkheadFallback"
    )
    @RateLimiter(
            name = "get-transaction-status",
            fallbackMethod = "getTransactionStatusRateLimiterFallback"
    )
    @CircuitBreaker(
            name = "get-transaction-status",
            fallbackMethod = "getTransactionStatusCircuitBreakerFallback"
    )
    public TransactionStatus getTransactionStatus(String transactionId) {
        String adminJwt = tokenService.getAdminJwtAsAuthorizationHeaderValue();
        String xTraceId = traceContext.getTraceId();
        return transactionApiClient.getTransactionStatus(transactionId, xTraceId, adminJwt).getBody();
    }

    private TransactionStatus getTransactionStatusRetryFallback(String transactionId, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String action = "get-transaction-status";
        String message = String.format("Error occurred during getting transaction status for transaction with id [%s]",
                transactionId);

        fallback.handleRetryFallback(action, message, e);

        return null;
    }

    private TransactionStatus getTransactionStatusBulkheadFallback(String transactionId, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String action = "get-transaction-status";
        String message = String.format("Error occurred during getting transaction status for transaction with id [%s]",
                transactionId);

        fallback.handleBulkheadFallback(action, message, e);

        return null;
    }

    private TransactionStatus getTransactionStatusRateLimiterFallback(String transactionId, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String action = "get-transaction-status";
        String message = String.format("Error occurred during getting transaction status for transaction with id [%s]",
                transactionId);

        fallback.handleRateLimiterFallback(action, message, e);

        return null;
    }

    private TransactionStatus getTransactionStatusCircuitBreakerFallback(String transactionId, Exception e) throws Exception {
        if(!fallbackExceptionPredicate.test(e))
            throw e;

        String action = "get-transaction-status";
        String message = String.format("Error occurred during getting transaction status for transaction with id [%s]",
                transactionId);

        fallback.handleCircuitBreakerFallback(action, message, e);

        return null;
    }

    private String getExceptionMessage(String subject, String transactionId) {
        return String.format("Received [%s] error response on transaction request with id: [%s]",
                subject, transactionId);
    }
}
