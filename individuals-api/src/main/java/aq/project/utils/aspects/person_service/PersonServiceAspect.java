package aq.project.utils.aspects.person_service;

import aq.project.dto.*;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class PersonServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.person_service.PersonService.login(..)) " +
            "&& args(individualsApiServiceLoginPersonRequestDto)")
    public Mono<IndividualsApiServiceTokenResponseDto> login(
            ProceedingJoinPoint pjp,
            @Valid IndividualsApiServiceLoginPersonRequestDto individualsApiServiceLoginPersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "login-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String loginUserEmail = individualsApiServiceLoginPersonRequestDto.getEmail();
        String preMainLogicLogMessage = String
                .format("Received request to login person with email: [%s]", loginUserEmail);
        String postSuccessMainLogicCallLogMessage = String
                .format("Success handle request to login person with email: [%s]", loginUserEmail);
        String postFailureMainLogicCallLogMessage = String
                .format("Error occurred during handle request to login person with email: [%s]", loginUserEmail);

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    @Around("execution(* aq.project.services.person_service.PersonService.create(..)) " +
            "&& args(individualsApiServiceCreatePersonRequestDto)")
    public Mono<IndividualsApiServiceTokenResponseDto> create(
            ProceedingJoinPoint pjp,
            @Valid IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String createUserEmail = individualsApiServiceCreatePersonRequestDto.getEmail();
        String preMainLogicLogMessage = String
                .format("Received request to create person with email: [%s]", createUserEmail);
        String postSuccessMainLogicCallLogMessage = String
                .format("Success handle request to create person with email: [%s]", createUserEmail);
        String postFailureMainLogicCallLogMessage = String
                .format("Error occurred during handle request to create person with email: [%s]", createUserEmail);

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                checkConstraints(individualsApiServiceCreatePersonRequestDto)
        );
    }

    private Supplier<Mono<Void>> checkConstraints(
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto
    ) {
        return () -> {
            String password = individualsApiServiceCreatePersonRequestDto.getPassword();
            String confirmPassword = individualsApiServiceCreatePersonRequestDto.getConfirmPassword();
            
            if (!password.equals(confirmPassword))
                throw new ConstraintViolationException("Input password and it's confirmation do not match.", null);

            return Mono.empty();
        };
    }

    @Around("execution(* aq.project.services.person_service.PersonService.update(..)) " +
            "&& args(individualsApiServiceUpdatePersonRequestDto)")
    public Mono<Void> update(
            ProceedingJoinPoint pjp,
            @Valid IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "update-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        UUID updateUserKeycloakId = individualsApiServiceUpdatePersonRequestDto.getPersonKeycloakId();
        String preMainLogicLogMessage = String
                .format("Received request to update person with id: [%s]", updateUserKeycloakId);
        String postSuccessMainLogicCallLogMessage = String
                .format("Success handle request to update person with id: [%s]", updateUserKeycloakId);
        String postFailureMainLogicCallLogMessage = String
                .format("Error occurred during handle request to update person with id: [%s]", updateUserKeycloakId);

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    @Around("execution(* aq.project.services.person_service.PersonService.delete(..)) " +
            "&& args(personKeycloakId)")
    public Mono<Void> delete(
            ProceedingJoinPoint pjp,
            @NotNull UUID personKeycloakId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "delete-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String
                .format("Received request to delete person with id: [%s]", personKeycloakId);
        String postSuccessMainLogicCallLogMessage = String
                .format("Success handle request to delete person with id: [%s]", personKeycloakId);
        String postFailureMainLogicCallLogMessage = String
                .format("Error occurred during handle request to delete person with id: [%s]", personKeycloakId);

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    @Around("execution(* aq.project.services.person_service.PersonService.getInfo(..)) " +
            "&& args(authentication)")
    public Mono<IndividualsApiServicePersonInfoResponseDto> getInfo(
            ProceedingJoinPoint pjp,
            @NotNull Authentication authentication
    ) throws Throwable {
//        Check for constraints
        checkConstraints(authentication);
//        Prepare handler metadata
        String actionName = "get-person-info";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String email = ((Jwt) authentication).getClaim("email");
        String preMainLogicLogMessage = String.format(
                "Received request to get person info with email: [%s]",
                email);
        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle request to get person info with email: [%s]",
                email);
        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle request to get person info with email: [%s]",
                email);

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    private void checkConstraints(Authentication authentication) {
        if(authentication == null) {
            throw new ConstraintViolationException("Access denied. Received request hasn't got valid access token", null);
        }

        if(authentication.getPrincipal() instanceof Jwt jwt) {
            if(Instant.now().isAfter(jwt.getExpiresAt())) {
                throw new ConstraintViolationException("Access denied. Access token was expired.", null);
            }
        } else {
            throw new ConstraintViolationException("Access denied. Only valid JWT access token required.", null);
        }
    }
}
