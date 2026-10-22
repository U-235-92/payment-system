package aq.project.services;

import aq.project.dto.PersonServiceOperation;
import aq.project.entities.Person;
import aq.project.entities.ServiceOperation;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.PersonRepository;
import aq.project.repositories.ServiceOperationRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

import static aq.project.dto.PersonServiceOperation.*;
import static aq.project.dto.UndoOperationStatus.COMPLETED;

@Service
@Validated
@RequiredArgsConstructor
public class UndoService {

    private final PersonRepository personRepository;
    private final ServiceOperationRepository serviceOperationRepository;

    @Transactional
    public void undoUpdatePerson(
            @NotNull UUID personKeycloakId
    ) {
        Person restoredPerson = personRepository.findUndoUpdateRevisionByPersonKeycloakId(personKeycloakId.toString())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Not found revision for person with keycloak id: [%s]", personKeycloakId)));

        personRepository.save(restoredPerson);
    }

    @Transactional
    public void undoDeletePerson(
            @NotNull UUID personKeycloakId
    ) {
        Person restoredPerson = personRepository.findUndoDeleteRevisionByPersonKeycloakId(personKeycloakId.toString())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Not found revision for person with keycloak id: [%s]", personKeycloakId)));

        restoredPerson.setId(null);
        restoredPerson.getAddress().setId(null);
        restoredPerson.getIndividual().setId(null);

        personRepository.save(restoredPerson);
    }

    public void checkUndoUpdate() {
        ServiceOperation serviceOperation = serviceOperationRepository.findLastServiceOperation(COMPLETED)
                .orElseThrow(() -> new EntityNotFoundException(
                        getOnNotFoundUndoOperationCallExceptionMessage()));

        PersonServiceOperation lastCompleteOperation = serviceOperation.getServiceOperation();
        PersonServiceOperation currentOperation = UNDO_UPDATE_PERSON;

        if(lastCompleteOperation.equals(CREATE_PERSON))
            throw new ProcessOperationException(
                    getOnMismatchOperationCallExceptionMessage(lastCompleteOperation, currentOperation));

        if(lastCompleteOperation.equals(currentOperation))
            throw new ProcessOperationException(
                    getOnUndoUndoOperationCallExceptionMessage(currentOperation));

        if(!lastCompleteOperation.equals(UPDATE_PERSON))
            throw new ProcessOperationException(
                    getOnNotExpectedUndoOperationCallExceptionMessage(
                            lastCompleteOperation, UNDO_UPDATE_PERSON, currentOperation));
    }

    public void checkUndoDelete() {
        ServiceOperation serviceOperation = serviceOperationRepository.findLastServiceOperation(COMPLETED)
                .orElseThrow(() -> new ProcessOperationException(
                        getOnNotFoundUndoOperationCallExceptionMessage()));

        PersonServiceOperation lastCompleteOperation = serviceOperation.getServiceOperation();
        PersonServiceOperation currentOperation = UNDO_DELETE_PERSON;

        if(lastCompleteOperation.equals(CREATE_PERSON))
            throw new ProcessOperationException(
                    getOnMismatchOperationCallExceptionMessage(lastCompleteOperation, currentOperation));

        if(lastCompleteOperation.equals(currentOperation))
            throw new ProcessOperationException(
                    getOnUndoUndoOperationCallExceptionMessage(currentOperation));

        if(!lastCompleteOperation.equals(DELETE_PERSON))
            throw new ProcessOperationException(
                    getOnNotExpectedUndoOperationCallExceptionMessage(
                            lastCompleteOperation, UNDO_DELETE_PERSON, currentOperation));
    }

    private String getOnMismatchOperationCallExceptionMessage(
            PersonServiceOperation lastOperation,
            PersonServiceOperation currentOperation
    ) {
        return String.format("Mismatch types of operations. Last operation: [%s]. Current operation: [%s]",
                lastOperation, currentOperation);
    }

    private String getOnNotExpectedUndoOperationCallExceptionMessage(
            PersonServiceOperation lastOperation,
            PersonServiceOperation expectedOperation,
            PersonServiceOperation currentOperation
    ) {
        return String.format("Illegal undo operation call. The last success operation: [%s] expected operation: [%s] but current: [%s]",
                lastOperation, expectedOperation, currentOperation);
    }

    private String getOnUndoUndoOperationCallExceptionMessage(
            PersonServiceOperation currentOperation
    ) {
        return String.format("Illegal undo operation call. Attempt to undo [%s] operation",
                currentOperation);
    }

    private String getOnNotFoundUndoOperationCallExceptionMessage() {
        return "No service operation found";
    }
}
