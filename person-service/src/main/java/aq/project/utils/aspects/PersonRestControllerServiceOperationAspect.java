package aq.project.utils.aspects;

import aq.project.entities.ServiceOperation;
import aq.project.services.OperationService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static aq.project.dto.PersonServiceOperation.*;
import static aq.project.dto.UndoOperationStatus.COMPLETED;
import static aq.project.dto.UndoOperationStatus.FAILED;

@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class PersonRestControllerServiceOperationAspect {

    private final OperationService operationService;

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.createPerson(..))")
    public void onSuccessCreatePerson() {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(CREATE_PERSON, COMPLETED, null);

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(
            value = "execution(* aq.project.controllers.PersonRestController.createPerson(..))",
            throwing = "e"
    )
    public void onFailCreatePerson(Exception e) {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(CREATE_PERSON, FAILED, e.getMessage());

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.deletePerson(..))")
    public void onSuccessDeletePerson() {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(DELETE_PERSON, COMPLETED, null);

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(
            value = "execution(* aq.project.controllers.PersonRestController.deletePerson(..))",
            throwing = "e"
    )
    public void onFailDeletePerson(Exception e) {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(DELETE_PERSON, FAILED, e.getMessage());

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.undoDeletePerson(..))")
    public void onSuccessUndoDeletePerson() {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(UNDO_DELETE_PERSON, COMPLETED, null);

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(
            value = "execution(* aq.project.controllers.PersonRestController.undoDeletePerson(..))",
            throwing = "e"
    )
    public void onFailUndoDeletePerson(Exception e) {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(UNDO_DELETE_PERSON, FAILED, e.getMessage());

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.updatePerson(..))")
    public void onSuccessUpdatePerson() {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(UPDATE_PERSON, COMPLETED, null);

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(
            value = "execution(* aq.project.controllers.PersonRestController.updatePerson(..))",
            throwing = "e"
    )
    public void onFailUpdatePerson(Exception e) {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(UPDATE_PERSON, FAILED, e.getMessage());

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.undoUpdatePerson(..))")
    public void onSuccessUndoUpdatePerson() {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(UNDO_UPDATE_PERSON, COMPLETED, null);

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(
            value = "execution(* aq.project.controllers.PersonRestController.undoUpdatePerson(..))",
            throwing = "e"
    )
    public void onFailUndoUpdatePerson(Exception e) {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(UNDO_UPDATE_PERSON, FAILED, e.getMessage());

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.getPersonInfo(..))")
    public void onSuccessGetPersonInfo() {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(GET_PERSON_INFO, COMPLETED, null);

        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(
            value = "execution(* aq.project.controllers.PersonRestController.getPersonInfo(..))",
            throwing = "e"
    )
    public void onFailGetPersonInfo(Exception e) {
        ServiceOperation serviceOperation = operationService
                .createServiceOperation(GET_PERSON_INFO, FAILED, e.getMessage());

        operationService.saveServiceOperation(serviceOperation);
    }
}
