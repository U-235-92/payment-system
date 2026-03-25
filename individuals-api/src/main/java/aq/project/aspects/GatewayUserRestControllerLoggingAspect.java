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
import reactor.core.publisher.Mono;
import static aq.project.util.telemetry.TelemetryUtils.*;
import static aq.project.util.log.LogUtils.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class GatewayUserRestControllerLoggingAspect {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    @Around("execution(* aq.project.controllers.GatewayUserRestController.*(..))")
    public Mono<?> traceGatewayUserRestControllerMethods(ProceedingJoinPoint pjp) throws Throwable {
        Tracer tracer = getTracer(applicationName, "gateway-user-rest-controller-logging-aspect-tracer", openTelemetry);
        Span span = getSpan(tracer, pjp);
        String traceId = getTraceId(span);
        String spanId = getSpanId(span);
        logBeforeCallMethod(span, pjp);
        return Mono.just((Mono<ResponseEntity<?>>) pjp.proceed())
                .flatMap(responseEntity -> responseEntity)
                .doOnSuccess(response -> logAfterCompletedCallMethod(span, pjp))
                .doOnError(exc -> logAspectError(pjp, exc, log, traceId, spanId))
                .doFinally(st -> span.end());
    }

    private void logBeforeCallMethod(Span span, ProceedingJoinPoint pjp) {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringType().getName();
        log.info(String.format("[%s-%s] Attempt of call method [%s.%s]", getTraceId(span), getSpanId(span),
                className, methodName));
    }

    private void logAfterCompletedCallMethod(Span span, ProceedingJoinPoint pjp) {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringType().getName();
        log.info(String.format("[%s-%s] Call of method [%s.%s] completed", getTraceId(span), getSpanId(span),
                className, methodName));
    }
}
