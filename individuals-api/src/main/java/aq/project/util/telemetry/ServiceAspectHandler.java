package aq.project.util.telemetry;

import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

import static aq.project.util.constants.CustomHttpHeaders.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceAspectHandler {

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    public <T> Mono<T> handle(
            ProceedingJoinPoint pjp,
            String tracerName,
            String serviceName,
            String actionName,
            String preMainLogicLogMessage,
            String postSuccessMainLogicCallLogMessage,
            String postFailureMainLogicCallLogMessage,
            Supplier<Mono<Void>> validation
    ) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(actionName).startSpan();
//        Prepare telemetry metadata
        String traceId = span.getSpanContext().getTraceId();
        String spanId = span.getSpanContext().getSpanId();
        Timer.Sample sample = applicationMetricsRegistry.startTimer();
//        Pre main logic log
        logOnPreMainLogic(traceId, spanId, serviceName, actionName, preMainLogicLogMessage);
//        Check for validation logic
        Mono<?> publisher = (validation != null) ? validation.get() : Mono.empty();
//        Validation logic call ---> Main logic call
        return publisher
                .then(Mono.defer(() -> {
                    try {
                        return (Mono<T>) pjp.proceed();
                    } catch (Throwable t) {
                        return Mono.error(t);
                    }
                }))
                .doOnError(e -> {
                    applicationMetricsRegistry.countAction(false, actionName);
                    logOnFailure(traceId, spanId, serviceName, actionName, postFailureMainLogicCallLogMessage, e);
                })
                .doOnSuccess(response -> {
                    long executionTime = applicationMetricsRegistry.finishTimer(sample, actionName);
                    applicationMetricsRegistry.countAction(true, actionName);
                    logOnSuccess(traceId, spanId, serviceName, actionName, postSuccessMainLogicCallLogMessage, executionTime);
                })
                .doFinally(signalType -> {
                    applicationMetricsRegistry.finishTimer(sample, actionName);
                    span.end();
                })
                .contextWrite(context -> context.put(X_TRACE_ID_HEADER, traceId));
    }

    private void logOnPreMainLogic(String traceId, String spanId, String service, String action, String description) {
        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, service, action, description);
    }

    private void logOnFailure(String traceId, String spanId, String service, String action, String description, Throwable t) {
        log.error("[{}-{}][{} -> {}]: {}. {}", traceId, spanId, service, action, description, t.getMessage());
    }

    private void logOnSuccess(String traceId, String spanId, String service, String action, String description, long executionTime) {
        log.info("[{}-{}][{} -> {}]: {} on {}ms", traceId, spanId, service, action, description, executionTime);
    }
}
