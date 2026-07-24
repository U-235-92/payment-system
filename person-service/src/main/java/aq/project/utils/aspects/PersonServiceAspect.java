package aq.project.utils.aspects;

import aq.project.entities.Individual;
import aq.project.entities.Person;
import aq.project.entities.UndoOperation;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class PersonServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.PersonService.createPerson(..)) && args(person)")
    public String createPersonAspect(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Person person
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String optEmail = Optional.ofNullable(person)
                .map(Person::getIndividual)
                .map(Individual::getEmail)
                .orElse("[invalid_email]");
        String preMainLogicLogMessage = String.format("Received request to create a person with email: %s",
                optEmail);
        String postSuccessMainLogicCallLogMessage = String.format("Success create a person with email: %s",
                optEmail);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during create a person with email: %s",
                optEmail);
//        Handler logic call
        return serviceAspectHandler.handle(
                String.class,
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

    @Around("execution(* aq.project.services.PersonService.getPersonByKeycloakId(..)) && args(keycloakId)")
    public Person getPersonByKeycloakIdAspect(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-person-by-keycloak-id";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String optKeycloakId = Optional.ofNullable(keycloakId)
                .orElse("[invalid_keycloak_id]");
        String preMainLogicLogMessage = String.format("Received request to get a person with keycloak_id: %s",
                optKeycloakId);
        String postSuccessMainLogicCallLogMessage = String.format("Success get a person with keycloak_id: %s",
                optKeycloakId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during get a person with keycloak_id: %s",
                optKeycloakId);
//        Handler logic call
        return serviceAspectHandler.handle(
                Person.class,
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

    @Around("execution(* aq.project.services.PersonService.updatePerson(..)) && args(from)")
    public void updatePersonAspect(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Person from
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "update-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String optEmail = Optional.ofNullable(from)
                .map(Person::getIndividual)
                .map(Individual::getEmail)
                .orElse("[invalid_email]");
        String preMainLogicLogMessage = String.format("Received request to update a person with email: %s",
                optEmail);
        String postSuccessMainLogicCallLogMessage = String.format("Success get update a person with email: %s",
                optEmail);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during update a person with email: %s",
                optEmail);
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

    @Around("execution(* aq.project.services.PersonService.undoUpdatePerson(..)) && args(undoOperation)")
    public void undoUpdatePersonAspect(
            ProceedingJoinPoint pjp,
            @NotNull @Valid UndoOperation undoOperation
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "undo-update-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String optUndoOperation = Optional.ofNullable(undoOperation)
                .map(UndoOperation::getOperation)
                .orElse("[invalid_undo_operation]");
        String preMainLogicLogMessage = String.format("Received request to undo operation: %s",
                optUndoOperation);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle undo operation: %s",
                optUndoOperation);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle undo operation: %s",
                optUndoOperation);
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

    @Around("execution(* aq.project.services.PersonService.deletePersonByKeycloakId(..)) && args(keycloakId)")
    public void deletePersonByKeycloakIdAspect(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "delete-person-by-keycloak-id";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String optKeycloakId = Optional.ofNullable(keycloakId)
                .orElse("[invalid_keycloak_id]");
        String preMainLogicLogMessage = String.format("Received request to delete a person with keycloak_id: %s",
                optKeycloakId);
        String postSuccessMainLogicCallLogMessage = String.format("Success delete a person with keycloak_id: %s",
                optKeycloakId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during delete a person with keycloak_id: %s",
                optKeycloakId);
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

    @Around("execution(* aq.project.services.PersonService.undoDeletePerson(..)) && args(undoOperation)")
    public void undoDeletePersonAspect(
            ProceedingJoinPoint pjp,
            @NotNull @Valid UndoOperation undoOperation
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "undo-delete-person";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String optUndoOperation = Optional.ofNullable(undoOperation)
                .map(UndoOperation::getOperation)
                .orElse("[invalid_undo_operation]");
        String preMainLogicLogMessage = String.format("Received request to undo operation: %s",
                optUndoOperation);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle undo operation: %s",
                optUndoOperation);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle undo operation: %s",
                optUndoOperation);
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
}
