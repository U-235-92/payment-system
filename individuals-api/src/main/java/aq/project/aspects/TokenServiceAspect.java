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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
@RequiredArgsConstructor
public class TokenServiceAspect {

    @Value("${spring.application.name}")
    private String tracerName;

    protected final Validator validator;

    protected final OpenTelemetry openTelemetry;

    protected final ApplicationMetricsRegistry applicationMetricsRegistry;

    @Timed(value = "individuals_api.refresh_token_time")
    @Around("execution(* aq.project.services.TokenService.refreshToken(..)) && args(refreshTokenDTO)")
    public Mono<ResponseTokenDTO> refreshToken(ProceedingJoinPoint pjp, RefreshTokenDTO refreshTokenDTO) throws Throwable {
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

            @Override
            protected void handleCustomViolations() {
                String refreshToken = refreshTokenDTO.getRefreshToken();
                Pattern pattern = getJwtPattern();
                Matcher matcher = pattern.matcher(refreshToken);
                if(!matcher.find())
                    throw new IllegalArgumentException(String.format("Invalid refresh token received: %s",
                            refreshToken));
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

    private Pattern getJwtPattern() {
        return Pattern.compile("^[eyJ][a-zA-Z0-9-_]+\\.[eyJ][a-zA-Z0-9-_]+\\.[a-zA-Z0-9-_]+$");
    }
}