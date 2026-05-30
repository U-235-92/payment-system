package aq.project.aspects.handlers;

import aq.project.util.metrics.ApplicationMetricsRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;

@Slf4j
@RequiredArgsConstructor(access = lombok.AccessLevel.PUBLIC)
public abstract class AbstractAspectHandler {

    private final Validator validator;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    protected abstract String getNullArgumentExceptionMessage();

    protected abstract String getPreMainLogicCallMethodMessage();

    protected abstract String getAfterSuccessMainLogicCallMessage();

    @SuppressWarnings("unchecked")
    public final <A, R> Mono<R> handleAspect(
            ProceedingJoinPoint pjp,
            A arg,
            String tracerName,
            String spanName,
            Consumer<ApplicationMetricsRegistry> onSuccess,
            Consumer<ApplicationMetricsRegistry> onException
    ) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(spanName).startSpan();
        try(Scope scope = span.makeCurrent()) {
            String traceId = span.getSpanContext().getTraceId();
            String spanId = span.getSpanContext().getSpanId();
            if(arg == null) {
                onException.accept(applicationMetricsRegistry);
                logException(getNullArgumentExceptionMessage(), traceId, spanId);
                throw new IllegalArgumentException(getNullArgumentExceptionMessage());
            }
            String methodName = pjp.getSignature().getName();
            String argName = ((MethodSignature) pjp.getSignature()).getParameterNames()[0].toLowerCase();
            if(String.class.isInstance(arg) && ( ((String) arg).isBlank() )) {
                onException.accept(applicationMetricsRegistry);
                String msg = String.format("Method %s has argument %s which value is null or blank",
                        methodName, argName);
                logException(msg, traceId, spanId);
                throw new IllegalArgumentException(msg);
            }
            if(String.class.isInstance(arg) && argName.contains("id")) {
                if(isInvalidUuid((String) arg)) {
                    String msg = "Wrong format of id, it has to be valid UUID value";
                    logException(msg, traceId, spanId);
                    onException.accept(applicationMetricsRegistry);
                    throw new IllegalArgumentException(msg);
                }
            }
            Set<ConstraintViolation<A>> violations = validator.validate(arg);
            if(!violations.isEmpty()) {
                ConstraintViolationException e = new ConstraintViolationException(violations);
                onException.accept(applicationMetricsRegistry);
                logException(e, traceId, spanId);
                throw e;
            }
            try {
                handleCustomViolations();
            } catch (Exception e) {
                onException.accept(applicationMetricsRegistry);
                logException(e, traceId, spanId);
                throw e;
            }
            log.info(String.format("[%s-%s]: %s", traceId, spanId, getPreMainLogicCallMethodMessage()));
            return ((Mono<R>) pjp.proceed())
                    .doOnError(e -> {
                        logException(e, traceId, spanId);
                        onException.accept(applicationMetricsRegistry);
                    })
                    .doOnSuccess(response -> {
                        log.info(String.format("[%s-%s]: %s", traceId, spanId, getAfterSuccessMainLogicCallMessage()));
                        onSuccess.accept(applicationMetricsRegistry);
                    })
                    .doFinally(signalType -> span.end());
        }
    }

    protected void handleCustomViolations() {}

    private void logException(Throwable e, String traceId, String spanId) {
        log.warn(String.format("[%s-%s]: %s", traceId, spanId, e.getMessage()));
    }

    private void logException(String msg, String traceId, String spanId) {
        log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
    }

    private boolean isInvalidUuid(String uuid) {
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        return !matcher.find();
    }
}
