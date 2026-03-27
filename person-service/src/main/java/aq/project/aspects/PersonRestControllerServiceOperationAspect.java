package aq.project.aspects;

import aq.project.entities.ServiceOperation;
import aq.project.repositories.ServiceOperationRepository;
import aq.project.services.OperationService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import static aq.project.util.constants.Statuses.*;
import static aq.project.util.constants.Operations.*;

@Aspect
@Component
@RequiredArgsConstructor
public class PersonRestControllerServiceOperationAspect {

    private final OperationService operationService;

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.createPerson(..))")
    public void onSuccessCreatePerson() {
        ServiceOperation serviceOperation = operationService.createServiceOperation(CREATE_PERSON_OP, COMPLETE_STATUS, null);
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.createPerson(..))", throwing = "e")
    public void onFailCreatePerson(Exception e) {
        ServiceOperation serviceOperation = operationService.createServiceOperation(CREATE_PERSON_OP, FAIL_STATUS, e.getMessage());
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.deletePersonByKeycloakId(..))")
    public void onSuccessDeletePersonByKeycloakId() {
        ServiceOperation serviceOperation = operationService.createServiceOperation(DELETE_PERSON_OP, COMPLETE_STATUS, null);
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.deletePersonByKeycloakId(..))", throwing = "e")
    public void onFailDeletePersonByKeycloakId(Exception e) {
        ServiceOperation serviceOperation = operationService.createServiceOperation(DELETE_PERSON_OP, FAIL_STATUS, e.getMessage());
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.undoDeletePerson(..))")
    public void onSuccessUndoDeletePerson() {
        ServiceOperation serviceOperation = operationService.createServiceOperation(UNDO_DELETE_PERSON_OP, COMPLETE_STATUS, null);
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.undoDeletePerson(..))", throwing = "e")
    public void onFailUndoDeletePerson(Exception e) {
        ServiceOperation serviceOperation = operationService.createServiceOperation(UNDO_DELETE_PERSON_OP, FAIL_STATUS, e.getMessage());
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.updatePerson(..))")
    public void onSuccessUpdatePerson() {
        ServiceOperation serviceOperation = operationService.createServiceOperation(UPDATE_PERSON_OP, COMPLETE_STATUS, null);
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.updatePerson(..))", throwing = "e")
    public void onFailUpdatePerson(Exception e) {
        ServiceOperation serviceOperation = operationService.createServiceOperation(UPDATE_PERSON_OP, FAIL_STATUS, e.getMessage());
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.undoUpdatePerson(..))")
    public void onSuccessUndoUpdatePerson() {
        ServiceOperation serviceOperation = operationService.createServiceOperation(UNDO_UPDATE_PERSON_OP, COMPLETE_STATUS, null);
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.undoUpdatePerson(..))", throwing = "e")
    public void onFailUndoUpdatePerson(Exception e) {
        ServiceOperation serviceOperation = operationService.createServiceOperation(UNDO_UPDATE_PERSON_OP, FAIL_STATUS, e.getMessage());
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.getPersonByKeycloakId(..))")
    public void onSuccessGetPersonByKeycloakId() {
        ServiceOperation serviceOperation = operationService.createServiceOperation(GET_PERSON_OP, COMPLETE_STATUS, null);
        operationService.saveServiceOperation(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.getPersonByKeycloakId(..))", throwing = "e")
    public void onFailGetPersonByKeycloakId(Exception e) {
        ServiceOperation serviceOperation = operationService.createServiceOperation(GET_PERSON_OP, FAIL_STATUS, e.getMessage());
        operationService.saveServiceOperation(serviceOperation);
    }
}
