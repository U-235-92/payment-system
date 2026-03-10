package aq.project.aspects;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PersonControllerLoggingAspect {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    @Around("execution(* aq.project.controllers.PersonRestController.*)")
    public ResponseEntity<?> aroundControllerMethods(ProceedingJoinPoint pjp) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(applicationName + ".person-controller-tracer");
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringType().getName();
        Span span = tracer.spanBuilder(className + "." + methodName).startSpan();
        String args = Arrays.toString(pjp.getArgs());
        log.debug(String.format("Call of method: %s.%s, args: %s", className, methodName, args));
        ResponseEntity<?> result = (ResponseEntity<?>) pjp.proceed();
        log.debug(String.format("Method: %s.%s was completed successfully", className, methodName));
        span.end();
        return result;
    }
}
