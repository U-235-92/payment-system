package aq.project.utils.aspects.token_service;

import aq.project.dto.IndividualsApiServiceTokenRequestDto;
import aq.project.dto.IndividualsApiServiceTokenResponseDto;
import aq.project.utils.telemetry.ServiceAspectHandler;
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

    @Around("execution(* aq.project.services.token_service.TokenService.refreshUserJwt(..)) " +
            "&& args(individualsApiServiceTokenRequestDto)")
    public Mono<IndividualsApiServiceTokenResponseDto> refreshUserJwt(
            ProceedingJoinPoint pjp,
            @NotNull @Valid IndividualsApiServiceTokenRequestDto individualsApiServiceTokenRequestDto
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
                checkConstraints(individualsApiServiceTokenRequestDto)
        );
    }

    private Supplier<Mono<Void>> checkConstraints(IndividualsApiServiceTokenRequestDto dto) {
        return () -> {
            if(dto == null)
                throw new ConstraintViolationException("Refresh token DTO is null", null);

            if(dto.getRefreshToken().isBlank())
                throw new ConstraintViolationException("Received request to refresh JWT token is blank", null);

            String refreshToken = dto.getRefreshToken();

            Pattern pattern = Pattern.compile("^[eyJ][a-zA-Z0-9-_]+\\.[eyJ][a-zA-Z0-9-_]+\\.[a-zA-Z0-9-_]+$");
            Matcher matcher = pattern.matcher(refreshToken);

            if(!matcher.find())
                throw new ConstraintViolationException(
                        String.format("Invalid refresh token received: %s", refreshToken), null);
            return Mono.empty();
        };
    }
}
