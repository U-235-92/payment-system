package aq.project.aspects;

import aq.project.dto.TokenRequestDto;
import aq.project.dto.TokenResponseDto;
import aq.project.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class TokenServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TokenService.refreshUserJwt(..)) " +
            "&& args(tokenRequestDto)")
    public Mono<TokenResponseDto> refreshUserJwt(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TokenRequestDto tokenRequestDto
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
                checkConstraints(tokenRequestDto)
        );
    }

    private Supplier<Mono<Void>> checkConstraints(TokenRequestDto tokenRequestDto) {
        return () -> {
            if(tokenRequestDto == null)
                throw new ConstraintViolationException("Refresh token DTO is null", null);

            if(tokenRequestDto.getRefreshToken().isBlank())
                throw new ConstraintViolationException("Received request to refresh JWT token is blank", null);

            String refreshToken = tokenRequestDto.getRefreshToken();

            Pattern pattern = Pattern.compile("^[eyJ][a-zA-Z0-9-_]+\\.[eyJ][a-zA-Z0-9-_]+\\.[a-zA-Z0-9-_]+$");
            Matcher matcher = pattern.matcher(refreshToken);

            if(!matcher.find())
                throw new ConstraintViolationException(
                        String.format("Invalid refresh token received: %s", refreshToken), null);
            return Mono.empty();
        };
    }
}