package aq.project.services;

import aq.project.entities.Person;
import aq.project.entities.ServiceOperation;
import aq.project.entities.UndoOperation;
import aq.project.exceptions.NotExpectedUndoOperationCallException;
import aq.project.exceptions.NotFoundRevisionException;
import aq.project.exceptions.NotFoundUndoOperationCallException;
import aq.project.repositories.PersonRepository;
import aq.project.repositories.ServiceOperationRepository;
import aq.project.repositories.UndoOperationRepository;
import aq.project.util.constants.Operations;
import aq.project.util.constants.Statuses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UndoService {

    private final PersonRepository personRepository;
    private final UndoOperationRepository undoOperationRepository;
    private final ServiceOperationRepository serviceOperationRepository;

    @Transactional
    public void undoOperation(String personKeycloakId, UUID undoOperationId, String undoOperationName) throws NotFoundRevisionException {
        if(undoOperationId == null) {
            String msg = String.format("Exception occurred while trying to [%s]: undoOperationId is null. " +
                    "Check UndoService.saveUndoOperation() method call.", undoOperationName);
            throw new RuntimeException(msg);
        }
        Person revision = personRepository.findUndoRevisionByKeycloakId(personKeycloakId);
        Person restored = new Person(revision);
        personRepository.delete(revision);
        personRepository.save(restored);
        undoOperationRepository.deleteById(undoOperationId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID saveUndoOperation(UndoOperation undoOperation) {
        return undoOperationRepository.save(undoOperation).getId();
    }

    public void checkUndoUpdate(UndoOperation undoOperation) throws NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        ServiceOperation serviceOperation = serviceOperationRepository.findLastServiceOperation(Statuses.COMPLETE_STATUS)
                .orElseThrow(() -> new NotFoundUndoOperationCallException(getOnNotFoundUndoOperationCallExceptionMessage()));
        String lastCompleteOperation = serviceOperation.getOperation();
        String currentOperation = undoOperation.getOperation();
        if(lastCompleteOperation.equals(Operations.CREATE_PERSON_OP))
            throw new NotExpectedUndoOperationCallException(getOnMismatchOperationCallExceptionMessage(lastCompleteOperation, currentOperation));
        if(lastCompleteOperation.equals(currentOperation))
            throw new NotExpectedUndoOperationCallException(getOnUndoUndoOperationCallExceptionMessage(currentOperation));
        if(!lastCompleteOperation.equals(Operations.UPDATE_PERSON_OP) || !currentOperation.equals(Operations.UNDO_UPDATE_PERSON_OP))
            throw new NotExpectedUndoOperationCallException(getOnNotExpectedUndoOperationCallExceptionMessage(lastCompleteOperation, Operations.UNDO_UPDATE_PERSON_OP, currentOperation));
    }

    public void checkUndoDelete(UndoOperation undoOperation) throws NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        ServiceOperation serviceOperation = serviceOperationRepository.findLastServiceOperation(Statuses.COMPLETE_STATUS)
                .orElseThrow(() -> new NotFoundUndoOperationCallException(getOnNotFoundUndoOperationCallExceptionMessage()));
        String lastCompleteOperation = serviceOperation.getOperation();
        String currentOperation = undoOperation.getOperation();
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
        return String.format("Illegal undo operation call. The last success operation: [%s] expected operation: [%s] but current: [%s]",
                lastOperation, expectedOperation, currentOperation);
    }

    private String getOnUndoUndoOperationCallExceptionMessage(String currentOperation) {
        return String.format("Illegal undo operation call. Attempt to undo [%s] operation", currentOperation);
    }

    private String getOnNotFoundUndoOperationCallExceptionMessage() {
        return "No service operation found";
    }
}
