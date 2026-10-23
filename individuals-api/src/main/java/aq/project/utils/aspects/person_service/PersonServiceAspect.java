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
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Supplier;

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class PersonServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.person_service.PersonService.loginPerson(..)) " +
            "&& args(loginPersonRequestDto)")
    public Mono<IndividualsApiServiceTokenResponseDto> loginPerson(
            ProceedingJoinPoint pjp,
            @NotNull @Valid IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "login-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String loginUserEmail = loginPersonRequestDto.getEmail();
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

    @Around("execution(* aq.project.services.person_service.PersonService.createPerson(..)) " +
            "&& args(createPersonRequestDto)")
    public Mono<IndividualsApiServiceTokenResponseDto> createPerson(
            ProceedingJoinPoint pjp,
            @NotNull @Valid IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String createUserEmail = createPersonRequestDto.getEmail();
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
                checkConstraints(createPersonRequestDto)
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

    @Around("execution(* aq.project.services.person_service.PersonService.updatePerson(..)) " +
            "&& args(jwtPersonKeycloakId, updatePersonRequestDto)")
    public Mono<Void> updatePerson(
            ProceedingJoinPoint pjp,
            @NotNull UUID jwtPersonKeycloakId,
            @NotNull @Valid IndividualsApiServiceUpdatePersonRequestDto updatePersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "update-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        UUID updateUserKeycloakId = updatePersonRequestDto.getPersonKeycloakId();
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

    @Around("execution(* aq.project.services.person_service.PersonService.deletePerson(..)) " +
            "&& args(jwtPersonKeycloakId, requestPersonKeycloakId)")
    public Mono<Void> deletePerson(
            ProceedingJoinPoint pjp,
            @NotNull UUID jwtPersonKeycloakId,
            @NotNull UUID requestPersonKeycloakId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "delete-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String
                .format("Received request to delete person with id: [%s]", requestPersonKeycloakId);
        String postSuccessMainLogicCallLogMessage = String
                .format("Success handle request to delete person with id: [%s]", requestPersonKeycloakId);
        String postFailureMainLogicCallLogMessage = String
                .format("Error occurred during handle request to delete person with id: [%s]", requestPersonKeycloakId);

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

    @Around("execution(* aq.project.services.person_service.PersonService.getPersonInfo(..)) " +
            "&& args(authentication)")
    public Mono<IndividualsApiServicePersonInfoResponseDto> getPersonInfo(
            ProceedingJoinPoint pjp,
            @NotNull Authentication authentication
    ) throws Throwable {
        if(!(authentication.getPrincipal() instanceof Jwt jwt))
            return Mono.error(new HttpClientErrorException(
                    HttpStatus.UNAUTHORIZED, "Access denied. Valid JWT required."));

//        Prepare handler metadata
        String actionName = "get-person-info";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String email = jwt.getClaim("email");
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
}
