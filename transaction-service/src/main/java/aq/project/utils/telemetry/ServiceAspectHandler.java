package aq.project.utils.telemetry;

import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceAspectHandler {

    private final OpenTelemetry openTelemetry;

    private final TraceContext traceContext;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    public <T> T handle(
            Class<T> returnResultClass,
            ProceedingJoinPoint pjp,
            String tracerName,
            String serviceName,
            String actionName,
            String preMainLogicLogMessage,
            String postSuccessMainLogicCallLogMessage,
            String postFailureMainLogicCallLogMessage,
            Supplier<Void> validation,
            Supplier<Void> before,
            Consumer<T> after,
            boolean cleanTraceContext
    ) throws Throwable {
//        Prepare telemetry metadata
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(actionName).startSpan();
        String traceId = (traceContext.getTraceId() == null)
                ? span.getSpanContext().getTraceId()
                : traceContext.getTraceId();
        String spanId = span.getSpanContext().getTraceId();
        Timer.Sample sample = applicationMetricsRegistry.startTimer();
        long executionTime;
        T result;
        try(Scope scope = span.makeCurrent()) {
//            Process telemetry
            logOnPreMainLogic(traceId, spanId, serviceName, actionName, preMainLogicLogMessage);
//            Validation logic call
            if(validation != null)
                validation.get();
//            Pre main logic call
            if(before != null)
                before.get();
//            Main logic call
            result = returnResultClass.cast(pjp.proceed());
//            Post main logic call
            if(after != null)
                after.accept(result);
        } catch (Throwable t) {
//            Telemetry on error
            logOnFailure(traceId, spanId, serviceName, actionName, postFailureMainLogicCallLogMessage, t);
            applicationMetricsRegistry.countAction(false, actionName);
            throw t;
        } finally {
//            Close all telemetry resources
            executionTime = applicationMetricsRegistry.finishTimer(sample, actionName);
            if(cleanTraceContext)
                traceContext.clean();
            span.end();
        }
//            Telemetry on success
        logOnSuccess(traceId, spanId, serviceName, actionName, postSuccessMainLogicCallLogMessage, executionTime);
        applicationMetricsRegistry.countAction(true, actionName);
        return result;
    }

    public void handle(
            ProceedingJoinPoint pjp,
            String tracerName,
            String serviceName,
            String actionName,
            String preMainLogicLogMessage,
            String postSuccessMainLogicCallLogMessage,
            String postFailureMainLogicCallLogMessage,
            Supplier<Void> validation,
            Supplier<Void> before,
            Supplier<Void> after,
            boolean cleanTraceContext
    ) throws Throwable {
//        Prepare telemetry metadata
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(actionName).startSpan();
        String traceId = (traceContext.getTraceId() == null)
                ? span.getSpanContext().getTraceId()
                : traceContext.getTraceId();
        String spanId = span.getSpanContext().getTraceId();
        Timer.Sample sample = applicationMetricsRegistry.startTimer();
        long executionTime;
        try(Scope scope = span.makeCurrent()) {
//            Process telemetry
            logOnPreMainLogic(traceId, spanId, serviceName, actionName, preMainLogicLogMessage);
//            Validation logic call
            if(validation != null)
                validation.get();
//            Pre main logic call
            if(before != null)
                before.get();
//            Main logic call
            pjp.proceed();
//            Post main logic call
            if(after != null)
                after.get();
        } catch (Throwable t) {
//            Telemetry on error
            logOnFailure(traceId, spanId, serviceName, actionName, postFailureMainLogicCallLogMessage, t);
            applicationMetricsRegistry.countAction(false, actionName);
            throw t;
        } finally {
//            Close all telemetry resources
            executionTime = applicationMetricsRegistry.finishTimer(sample, actionName);
            if(cleanTraceContext)
                traceContext.clean();
            span.end();
        }
//            Telemetry on success
        logOnSuccess(traceId, spanId, serviceName, actionName, postSuccessMainLogicCallLogMessage, executionTime);
        applicationMetricsRegistry.countAction(true, actionName);
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
