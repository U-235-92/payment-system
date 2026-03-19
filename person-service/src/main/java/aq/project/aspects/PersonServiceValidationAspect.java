package aq.project.aspects;

import aq.project.entities.Person;
import aq.project.entities.ServiceOperation;
import aq.project.entities.UndoEvent;
import aq.project.exceptions.NotExpectedUndoOperationCallException;
import aq.project.exceptions.NotFoundUndoOperationCallException;
import aq.project.repositories.ServiceOperationRepository;
import aq.project.util.constants.Operations;
import aq.project.util.constants.Statuses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Aspect
@Validated
@Component
@RequiredArgsConstructor
public class PersonServiceValidationAspect {

    private final ServiceOperationRepository serviceOperationRepository;

    @Before("execution(* aq.project.services.PersonService.createPerson(..)) && args(person)")
    public void checkCreatePerson(@Valid Person person) {}

    @Before("execution(* aq.project.services.PersonService.getPersonByKeycloakId(..)) && args(keycloakId)")
    public void checkGetPersonByKeycloakId(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {}

    @Before("execution(* aq.project.services.PersonService.updatePerson(..)) && args(from)")
    public void checkUpdatePerson(@Valid Person from) {}

    @Before("execution(* aq.project.services.PersonService.undoUpdatePerson(..)) && args(undoEvent)")
    public void checkUndoUpdatePerson(@Valid UndoEvent undoEvent) throws NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        ServiceOperation serviceOperation = serviceOperationRepository.findLastServiceOperation(Statuses.COMPLETE_STATUS)
                .orElseThrow(() -> new NotFoundUndoOperationCallException(getOnNotFoundUndoOperationCallExceptionMessage()));
        String lastCompleteOperation = serviceOperation.getOperation();
        String currentOperation = undoEvent.getOperation();
        if(lastCompleteOperation.equals(Operations.CREATE_PERSON_OP))
            throw new NotExpectedUndoOperationCallException(getOnMismatchOperationCallExceptionMessage(lastCompleteOperation, currentOperation));
        if(lastCompleteOperation.equals(currentOperation))
            throw new NotExpectedUndoOperationCallException(getOnUndoUndoOperationCallExceptionMessage(currentOperation));
        if(!lastCompleteOperation.equals(Operations.UPDATE_PERSON_OP) || !currentOperation.equals(Operations.UNDO_UPDATE_PERSON_OP))
            throw new NotExpectedUndoOperationCallException(getOnNotExpectedUndoOperationCallExceptionMessage(lastCompleteOperation, Operations.UNDO_UPDATE_PERSON_OP, currentOperation));
    }

    @Before("execution(* aq.project.services.PersonService.deletePersonByKeycloakId(..)) && args(keycloakId)")
    public void checkDeletePersonByKeycloakId(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {}

    @Before("execution(* aq.project.services.PersonService.undoDeletePersonByKeycloakId(..)) && args(undoEvent)")
    public void checkUndoDeletePersonByPersonId(@Valid UndoEvent undoEvent) throws NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        ServiceOperation serviceOperation = serviceOperationRepository.findLastServiceOperation(Statuses.COMPLETE_STATUS)
                .orElseThrow(() -> new NotFoundUndoOperationCallException(getOnNotFoundUndoOperationCallExceptionMessage()));
        String lastCompleteOperation = serviceOperation.getOperation();
        String currentOperation = undoEvent.getOperation();
        if(lastCompleteOperation.equals(Operations.CREATE_PERSON_OP))
            throw new NotExpectedUndoOperationCallException(getOnMismatchOperationCallExceptionMessage(lastCompleteOperation, currentOperation));
        if(lastCompleteOperation.equals(currentOperation))
            throw new NotExpectedUndoOperationCallException(getOnUndoUndoOperationCallExceptionMessage(currentOperation));
        if(!lastCompleteOperation.equals(Operations.DELETE_PERSON_OP) || !currentOperation.equals(Operations.UNDO_DELETE_PERSON_OP))
            throw new NotExpectedUndoOperationCallException(getOnNotExpectedUndoOperationCallExceptionMessage(lastCompleteOperation, Operations.UNDO_UPDATE_PERSON_OP, currentOperation));
    }

    private String getOnMismatchOperationCallExceptionMessage(String lastOperation, String currentOperation) {
        return String.format("Mismatch types of operations. Last operation: [%s]. Current operation: [%s]",
                lastOperation, currentOperation);
    }

    private String getOnNotExpectedUndoOperationCallExceptionMessage(String lastOperation, String expectedOperation, String currentOperation) {
        return String.format("Illegal undo operation call. " +
                "The last success operation: [%s] " +
                "expected operation: [%s] " +
                "but current: [%s]", lastOperation, expectedOperation, currentOperation);
    }

    private String getOnUndoUndoOperationCallExceptionMessage(String currentOperation) {
        return String.format("Illegal undo operation call. Attempt to undo [%s] operation", currentOperation);
    }

    private String getOnNotFoundUndoOperationCallExceptionMessage() {
        return "No service operation found";
    }
}
