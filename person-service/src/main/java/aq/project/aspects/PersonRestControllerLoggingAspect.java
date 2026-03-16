package aq.project.aspects;

import aq.project.util.observability.ObserverUtils;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PersonRestControllerLoggingAspect {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    @Around("execution(* aq.project.controllers.PersonRestController.*)")
    public ResponseEntity<?> aroundControllerMethods(ProceedingJoinPoint pjp) {
        Tracer tracer = openTelemetry.getTracer(applicationName + ".person-controller-tracer");
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringType().getName();
        Span span = tracer.spanBuilder(className + "." + methodName).startSpan();
        String traceId = ObserverUtils.getTraceId(span);
        String spanId = ObserverUtils.getSpanId(span);
        String beforeCallMethodLogMessage = String.format("[%s-%s] call of method: %s.%s", traceId, spanId, className, methodName);
        log.debug(beforeCallMethodLogMessage);
        ResponseEntity<?> result = null;
        try {
            result = (ResponseEntity<?>) pjp.proceed();
            String afterCallMethodLogMessage = String.format("[%s-%s] method: %s.%s was completed successfully", traceId, spanId, className, methodName);
            log.debug(afterCallMethodLogMessage);
            span.setStatus(StatusCode.OK);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
        return result;
    }
}
