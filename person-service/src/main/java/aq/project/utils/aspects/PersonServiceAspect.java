package aq.project.utils.aspects;

import aq.project.dto.*;
import aq.project.utils.telemetry.ServiceAspectHandler;
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

import java.util.UUID;

@Aspect
@Order(2)
@Component
@Validated
@RequiredArgsConstructor
public class PersonServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.PersonService.createPerson(..)) " +
            "&& args(personServiceCreatePersonRequestDto)")
    public UUID createPerson(
            ProceedingJoinPoint pjp,
            @NotNull @Valid PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-person";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String email = personServiceCreatePersonRequestDto.getEmail();

        String preMainLogicLogMessage = String.format(
                "Received request to create a person with email: %s", email);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success create a person with email: %s", email);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during create a person with email: %s", email);

//        Handler logic call
        return serviceAspectHandler.handle(
                UUID.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }

    @Around("execution(* aq.project.services.PersonService.updatePerson(..)) " +
            "&& args(personServiceUpdatePersonRequestDto)")
    public void updatePerson(
            ProceedingJoinPoint pjp,
            @NotNull @Valid PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "update-person";
        String tracerName = serviceName + "." + actionName + "-tracer";

        UUID personKeycloakId = personServiceUpdatePersonRequestDto.getPersonKeycloakId();

        String preMainLogicLogMessage = String.format(
                "Received request to update a person with keycloak id: %s", personKeycloakId);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success get update a person with keycloak id: %s", personKeycloakId);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during update a person with keycloak id: %s", personKeycloakId);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }

    @Around("execution(* aq.project.services.PersonService.undoUpdatePerson(..)) " +
            "&& args(personServiceUndoUpdatePersonRequestDto)")
    public void undoUpdatePerson(
            ProceedingJoinPoint pjp,
            @NotNull @Valid PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "undo-update-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String operation = "undo update person";

        String preMainLogicLogMessage = String.format(
                "Received request to undo operation: %s", operation);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle undo operation: %s", operation);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle undo operation: %s", operation);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }

    @Around("execution(* aq.project.services.PersonService.deletePerson(..)) " +
            "&& args(personKeycloakId)")
    public void deletePerson(
            ProceedingJoinPoint pjp,
            @NotNull UUID personKeycloakId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "delete-person";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = String.format(
                "Received request to delete a person with keycloak_id: %s", personKeycloakId);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success delete a person with keycloak_id: %s", personKeycloakId);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during delete a person with keycloak_id: %s", personKeycloakId);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }

    @Around("execution(* aq.project.services.PersonService.undoDeletePerson(..)) " +
            "&& args(personServiceUndoDeletePersonRequestDto)")
    public void undoDeletePerson(
            ProceedingJoinPoint pjp,
            @NotNull @Valid PersonServiceUndoDeletePersonRequestDto personServiceUndoDeletePersonRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "undo-delete-person";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String operation = "undo delete person";

        String preMainLogicLogMessage = String.format(
                "Received request to undo operation: %s", operation);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle undo operation: %s", operation);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle undo operation: %s", operation);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }

    @Around("execution(* aq.project.services.PersonService.getPersonInfo(..)) " +
            "&& args(personKeycloakId)")
    public PersonServicePersonInfoResponseDto getPersonInfo(
            ProceedingJoinPoint pjp,
            @NotNull UUID personKeycloakId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-person-info";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = String.format(
                "Received request to get person info with keycloak_id: %s", personKeycloakId);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success get person info with keycloak_id: %s", personKeycloakId);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during get person info with keycloak_id: %s", personKeycloakId);

//        Handler logic call
        return serviceAspectHandler.handle(
                PersonServicePersonInfoResponseDto.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }
}
