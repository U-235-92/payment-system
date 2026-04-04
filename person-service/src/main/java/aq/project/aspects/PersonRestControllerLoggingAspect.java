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

    @Around("execution(* aq.project.controllers.PersonRestController.*(..))")
    public Object aroundControllerMethods(ProceedingJoinPoint pjp) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(applicationName + ".person-controller-tracer");
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringType().getName();
        Span span = tracer.spanBuilder(className + "." + methodName).startSpan();
        String traceId = ObserverUtils.getTraceId(span);
        String spanId = ObserverUtils.getSpanId(span);
        String beforeCallMethodLogMessage = String.format("[%s-%s] call of method: %s.%s", traceId, spanId, className, methodName);
        log.info(beforeCallMethodLogMessage);
        Object result = null;
        try {
            result = pjp.proceed();
            String afterCallMethodLogMessage = String.format("[%s-%s] method: %s.%s was completed successfully", traceId, spanId, className, methodName);
            log.info(afterCallMethodLogMessage);
            span.setStatus(StatusCode.OK);
        } finally {
            span.end();
        }
        return result;
    }
}
