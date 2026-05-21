package aq.project.aspects;

import aq.project.aspects.handlers.AbstractAspectHandler;
import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.util.metrics.ApplicationMetricsRegistry;
import io.micrometer.core.annotation.Timed;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Component
@RequiredArgsConstructor
public class TokenRestControllerAspect {

    @Value("${spring.application.name}")
    private String tracerName;

    protected final Validator validator;

    protected final OpenTelemetry openTelemetry;

    protected final ApplicationMetricsRegistry applicationMetricsRegistry;

    @Timed(value = "individuals_api.refresh_token_time")
    @Around("execution(* aq.project.controllers.TokenRestController.refreshToken(..)) && args(refreshTokenDTO)")
    public Mono<ResponseEntity<ResponseTokenDTO>> refreshToken(ProceedingJoinPoint pjp, RefreshTokenDTO refreshTokenDTO) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Refresh token dto is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                return "Received refresh token dto request";
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                return "Handle of refresh token dto completed successfully";
            }
        };
        String spanName = "refresh_token";
        return handler.handleAspect(
                pjp,
                refreshTokenDTO,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessRefreshTokenCounter,
                ApplicationMetricsRegistry::incrementFailRefreshTokenCounter);
    }
}
