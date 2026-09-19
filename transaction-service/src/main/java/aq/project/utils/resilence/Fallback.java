package aq.project.utils.resilence;

import aq.project.exceptions.FallbackOperationException;
import aq.project.utils.telemetry.TraceContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Fallback {

    @Value("${spring.application.name}")
    private String serviceName;

    private final OpenTelemetry openTelemetry;

    private final TraceContext traceContext;

    public void handleRetryFallback(String action, String errorMessage, Exception cause) {
        String actionName = String.format("handle-%s-retry-fallback", action);
        String tracerName = serviceName + "." + actionName + "-tracer";

        Object[] telemetry = startSpan(tracerName, actionName);

        Span span = (Span) telemetry[0];
        String traceId = (String) telemetry[1];
        String spanId = (String) telemetry[2];

        String message = isNullOrBlank(errorMessage)
                ? "Error occurred while attempt to call foreign service"
                : String.format("%s: error occurred while attempt to call foreign service", errorMessage);

        logError(traceId, spanId, actionName, message, cause);

        span.end();

        throw new FallbackOperationException(message, cause);
    }

    public void handleBulkheadFallback(String action, String errorMessage, Exception cause) {
        String actionName = String.format("handle-%s-retry-fallback", action);
        String tracerName = serviceName + "." + actionName + "-tracer";

        Object[] telemetry = startSpan(tracerName, actionName);

        Span span = (Span) telemetry[0];
        String traceId = (String) telemetry[1];
        String spanId = (String) telemetry[2];

        String message = isNullOrBlank(errorMessage)
                ? "Exceeded limit of allowed resources to call foreign service"
                : String.format("%s: exceeded limit of allowed resources to call foreign service", errorMessage);

        logError(traceId, spanId, actionName, message, cause);

        span.end();

        throw new FallbackOperationException(message, cause);
    }

    public void handleRateLimiterFallback(String action, String errorMessage, Exception cause) {
        String actionName = String.format("handle-%s-retry-fallback", action);
        String tracerName = serviceName + "." + actionName + "-tracer";

        Object[] telemetry = startSpan(tracerName, actionName);

        Span span = (Span) telemetry[0];
        String traceId = (String) telemetry[1];
        String spanId = (String) telemetry[2];

        String message = isNullOrBlank(errorMessage)
                ? "Exceeded count of allowed requests to call current endpoint"
                : String.format("%s: exceeded count of allowed requests to call current endpoint", errorMessage);

        logError(traceId, spanId, actionName, message, cause);

        span.end();

        throw new FallbackOperationException(message, cause);
    }

    public void handleCircuitBreakerFallback(String action, String errorMessage, Exception cause) {
        String actionName = String.format("handle-%s-retry-fallback", action);
        String tracerName = serviceName + "." + actionName + "-tracer";

        Object[] telemetry = startSpan(tracerName, actionName);

        Span span = (Span) telemetry[0];
        String traceId = (String) telemetry[1];
        String spanId = (String) telemetry[2];

        String message = isNullOrBlank(errorMessage)
                ? "Foreign service is not available"
                : String.format("%s: foreign service is not available", errorMessage);

        logError(traceId, spanId, actionName, message, cause);

        span.end();

        throw new FallbackOperationException(message, cause);
    }

    private Object[] startSpan(String tracerName, String actionName) {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(actionName).startSpan();
        String traceId = (traceContext.getTraceId() == null)
                ? span.getSpanContext().getTraceId()
                : traceContext.getTraceId();
        String spanId = span.getSpanContext().getTraceId();
        return new Object[]{ span, traceId, spanId };
    }

    private boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    private void logError(String traceId, String spanId, String action, String message, Exception exc) {
        log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                traceId, spanId, serviceName, action, message, exc.getMessage());
    }
}
