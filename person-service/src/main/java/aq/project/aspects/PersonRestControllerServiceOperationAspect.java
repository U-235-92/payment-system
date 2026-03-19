package aq.project.aspects;

import aq.project.entities.ServiceOperation;
import aq.project.repositories.ServiceOperationRepository;
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

    private final ServiceOperationRepository serviceOperationRepository;

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.createPerson(..))")
    public void onSuccessCreatePerson() {
        ServiceOperation serviceOperation = createServiceOperation(CREATE_PERSON_OP, COMPLETE_STATUS, null);
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.createPerson(..))", throwing = "e")
    public void onFailCreatePerson(Exception e) {
        ServiceOperation serviceOperation = createServiceOperation(CREATE_PERSON_OP, FAIL_STATUS, e.getMessage());
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.deletePersonByKeycloakId(..))")
    public void onSuccessDeletePersonByKeycloakId() {
        ServiceOperation serviceOperation = createServiceOperation(DELETE_PERSON_OP, COMPLETE_STATUS, null);
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.deletePersonByKeycloakId(..))", throwing = "e")
    public void onFailDeletePersonByKeycloakId(Exception e) {
        ServiceOperation serviceOperation = createServiceOperation(DELETE_PERSON_OP, FAIL_STATUS, e.getMessage());
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.undoDeletePersonByKeycloakId(..))")
    public void onSuccessUndoDeletePersonByKeycloakId() {
        ServiceOperation serviceOperation = createServiceOperation(UNDO_DELETE_PERSON_OP, COMPLETE_STATUS, null);
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.undoDeletePersonByKeycloakId(..))", throwing = "e")
    public void onFailUndoDeletePersonByKeycloakId(Exception e) {
        ServiceOperation serviceOperation = createServiceOperation(UNDO_DELETE_PERSON_OP, FAIL_STATUS, e.getMessage());
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.updatePerson(..))")
    public void onSuccessUpdatePerson() {
        ServiceOperation serviceOperation = createServiceOperation(UPDATE_PERSON_OP, COMPLETE_STATUS, null);
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.updatePerson(..))", throwing = "e")
    public void onFailUpdatePerson(Exception e) {
        ServiceOperation serviceOperation = createServiceOperation(UPDATE_PERSON_OP, FAIL_STATUS, e.getMessage());
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.undoUpdatePerson(..))")
    public void onSuccessUndoUpdatePerson() {
        ServiceOperation serviceOperation = createServiceOperation(UNDO_UPDATE_PERSON_OP, COMPLETE_STATUS, null);
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.undoUpdatePerson(..))", throwing = "e")
    public void onFailUndoUpdatePerson(Exception e) {
        ServiceOperation serviceOperation = createServiceOperation(UNDO_UPDATE_PERSON_OP, FAIL_STATUS, e.getMessage());
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.getPersonByKeycloakId(..))")
    public void onSuccessGetPersonByKeycloakId() {
        ServiceOperation serviceOperation = createServiceOperation(GET_PERSON_OP, COMPLETE_STATUS, null);
        serviceOperationRepository.save(serviceOperation);
    }

    @AfterThrowing(value = "execution(* aq.project.controllers.PersonRestController.getPersonByKeycloakId(..))", throwing = "e")
    public void onFailGetPersonByKeycloakId(Exception e) {
        ServiceOperation serviceOperation = createServiceOperation(GET_PERSON_OP, FAIL_STATUS, e.getMessage());
        serviceOperationRepository.save(serviceOperation);
    }

    private ServiceOperation createServiceOperation(String operation, String status, String description) {
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setDescription(description);
        serviceOperation.setOperation(operation);
        serviceOperation.setStatus(status);
        return serviceOperation;
    }
}
