package aq.project.util.aspects;

import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.util.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class TokenServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TokenService.refreshUserJwt(..)) && args(refreshTokenDTO)")
    public Mono<ResponseTokenDTO> refreshUserJwt(
            ProceedingJoinPoint pjp,
            @NotNull @Valid RefreshTokenDTO refreshTokenDTO
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "refresh-token";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to refresh JWT token";
        String postSuccessMainLogicCallLogMessage = "Success handle request to refresh JWT token";
        String postFailureMainLogicCallLogMessage = "Error occurred during handle request to refresh JWT token";
//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                checkConstraints(refreshTokenDTO)
        );
    }

    private Supplier<Mono<Void>> checkConstraints(RefreshTokenDTO refreshTokenDTO) {
        return () -> {
            if(refreshTokenDTO == null)
                throw new IllegalArgumentException("Refresh token DTO is null", null);
            if(refreshTokenDTO.getRefreshToken().isBlank())
                throw new IllegalArgumentException("Received request to refresh JWT token is blank");

            String refreshToken = refreshTokenDTO.getRefreshToken();
            Pattern pattern = Pattern.compile("^[eyJ][a-zA-Z0-9-_]+\\.[eyJ][a-zA-Z0-9-_]+\\.[a-zA-Z0-9-_]+$");
            Matcher matcher = pattern.matcher(refreshToken);
            if(!matcher.find())
                throw new IllegalArgumentException(String.format("Invalid refresh token received: %s", refreshToken));
            return Mono.empty();
        };
    }
}
