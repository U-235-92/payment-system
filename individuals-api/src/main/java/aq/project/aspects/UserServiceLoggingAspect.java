package aq.project.aspects;

import aq.project.dto.CreateUserDTO;
import aq.project.dto.LoginUserDTO;
import aq.project.dto.UpdateUserDTO;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import static aq.project.util.telemetry.TelemetryUtils.*;
import static aq.project.util.log.LogUtils.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UserServiceLoggingAspect {

    private static final String USER_SERVICE_TRACER = "user-service-logging-aspect-tracer";

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    @Around(value = "execution(* aq.project.services.UserService.createUser(..))")
    public Mono<?> createUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        CreateUserDTO createUserDTO = (CreateUserDTO) pjp.getArgs()[0];
        Tracer tracer = getTracer(applicationName, USER_SERVICE_TRACER, openTelemetry);
        Span span = getSpan(tracer, pjp);
        String traceId = getTraceId(span);
        String spanId = getSpanId(span);
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("[%s-%s] User with email [%s] was created successfully", traceId, spanId, createUserDTO.getIndividualData().getEmail())))
                .doOnError(exc -> logAspectError(pjp, exc, log, traceId, spanId))
                .doFinally(st -> span.end());
    }

    @Around(value = "execution(* aq.project.services.UserService.loginUser(..))")
    public Mono<?> loginUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        LoginUserDTO loginUserDTO = (LoginUserDTO) pjp.getArgs()[0];
        Tracer tracer = getTracer(applicationName, USER_SERVICE_TRACER, openTelemetry);
        Span span = getSpan(tracer, pjp);
        String traceId = getTraceId(span);
        String spanId = getSpanId(span);
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("[%s-%s] User with email [%s] was logged in successfully.", traceId, spanId, loginUserDTO.getEmail())))
                .doOnError(exc -> logAspectError(pjp, exc, log, traceId, spanId))
                .doFinally(st -> span.end());
    }

    @Around(value = "execution(* aq.project.services.UserService.updateUser(..))")
    public Mono<?> updateUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        UpdateUserDTO updateUserDTO = (UpdateUserDTO) pjp.getArgs()[0];
        Tracer tracer = getTracer(applicationName, USER_SERVICE_TRACER, openTelemetry);
        Span span = getSpan(tracer, pjp);
        String traceId = getTraceId(span);
        String spanId = getSpanId(span);
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("[%s-%s] User with keycloakId [%s] was updated successfully.", traceId, spanId, updateUserDTO.getKeycloakUserId())))
                .doOnError(exc -> logAspectError(pjp, exc, log, traceId, spanId))
                .doFinally(st -> span.end());
    }

    @Around(value = "execution(* aq.project.services.UserService.deleteUserByKeycloakId(..))")
    public Mono<?> deleteUserByKeycloakIdAspect(ProceedingJoinPoint pjp) throws Throwable {
        String keycloakUserId = (String) pjp.getArgs()[0];
        Tracer tracer = getTracer(applicationName, USER_SERVICE_TRACER, openTelemetry);
        Span span = getSpan(tracer, pjp);
        String traceId = getTraceId(span);
        String spanId = getSpanId(span);
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("[%s-%s] User with keycloakId [%s] was deleted successfully.", traceId, spanId, keycloakUserId)))
                .doOnError(exc -> logAspectError(pjp, exc, log, traceId, spanId))
                .doFinally(st -> span.end());
    }

    @Around(value = "execution(* aq.project.services.UserService.getUserInfoResponseDTO(..))")
    public Mono<?> getUserInfoResponseDTOAspect(ProceedingJoinPoint pjp) throws Throwable {
        Authentication authentication =  (Authentication) pjp.getArgs()[0];
        String[] email = new String[1];
        if(authentication.getPrincipal() instanceof Jwt jwt)
            email[0] = jwt.getClaim("email");
        Tracer tracer = getTracer(applicationName, USER_SERVICE_TRACER, openTelemetry);
        Span span = getSpan(tracer, pjp);
        String traceId = getTraceId(span);
        String spanId = getSpanId(span);
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("[%s-%s] User with email [%s] requested user info successfully.", traceId, spanId, email[0])))
                .doOnError(exc -> logAspectError(pjp, exc, log, traceId, spanId))
                .doFinally(st -> span.end());
    }
}
