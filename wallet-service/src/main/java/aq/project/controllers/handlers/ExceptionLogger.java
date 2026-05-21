package aq.project.controllers.handlers;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class ExceptionLogger {

    @Value("${spring.application.name}")
    private String tracerName;

    private final OpenTelemetry openTelemetry;

    protected void logException(Exception exception, HttpStatus status) {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("exception-span").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setStatus(StatusCode.ERROR, String.format("%d: %s", status.value(), status.getReasonPhrase()));
            span.recordException(exception);
            String traceId = span.getSpanContext().getTraceId();
            String spanId =  span.getSpanContext().getSpanId();
            String exceptionClassName = exception.getClass().getSimpleName();
            String exceptionCause = exception.getCause() == null ? "" : "Cause: " + exception.getCause().getClass().getName() + ": " + exception.getCause().getMessage();
            String exceptionMessage = exception.getMessage() + "; " + exceptionCause;
            String logMessage = String.format("[%s-%s] %s occurred at: %s", traceId, spanId, exceptionClassName, exceptionMessage);
            log.warn(logMessage);
        } finally {
            span.end();
        }
    }
}
