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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import static aq.project.util.telemetry.TelemetryUtils.*;
import static aq.project.util.log.LogUtils.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TokenServiceLoggingAspect {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    @Around(value = "execution(* aq.project.services.TokenService.refreshToken(..))")
    public Mono<?> refreshTokenAspect(ProceedingJoinPoint pjp) throws Throwable {
        Tracer tracer = getTracer(applicationName, "token-service-logging-aspect-tracer", openTelemetry);
        Span span = getSpan(tracer, pjp);
        String traceId = getTraceId(span);
        String spanId = getSpanId(span);
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("[%s-%s] Token was refreshed successfully.", getTraceId(span), getSpanId(span))))
                .doOnError(exc -> logAspectError(pjp, exc, log, traceId, spanId))
                .doFinally(st -> span.end());
    }
}
