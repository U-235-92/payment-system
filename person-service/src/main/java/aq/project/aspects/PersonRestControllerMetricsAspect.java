package aq.project.aspects;

import aq.project.metrics.ApplicationMetricRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PersonRestControllerMetricsAspect {

    private final ApplicationMetricRegistry applicationMetricRegistry;

    @After("execution(* aq.project.controllers.PersonRestController.createPerson(..))")
    public void onSuccessCreatePerson() {
        applicationMetricRegistry.incrementSuccessCreatePersonCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.createPerson(..))")
    public void onFailCreatePerson() {
        applicationMetricRegistry.incrementFailCreatePersonCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.deletePersonByKeycloakId(..))")
    public void onSuccessDeletePersonByKeycloakId() {
        applicationMetricRegistry.incrementSuccessDeletePersonByKeycloakIdCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.deletePersonByKeycloakId(..))")
    public void onFailDeletePersonByKeycloakId() {
        applicationMetricRegistry.incrementFailDeletePersonByKeycloakIdCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.undoDeletePerson(..))")
    public void onSuccessUndoDeletePerson() {
        applicationMetricRegistry.incrementSuccessUndoDeletePersonCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.undoDeletePerson(..))")
    public void onFailUndoDeletePerson() {
        applicationMetricRegistry.incrementFailUndoDeletePersonCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.updatePerson(..))")
    public void onSuccessUpdatePerson() {
        applicationMetricRegistry.incrementSuccessUpdatePersonCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.updatePerson(..))")
    public void onFailUpdatePerson() {
        applicationMetricRegistry.incrementFailUpdatePersonCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.undoUpdatePerson(..))")
    public void onSuccessUndoUpdatePerson() {
        applicationMetricRegistry.incrementSuccessUndoUpdatePersonCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.undoUpdatePerson(..))")
    public void onFailUndoUpdatePerson() {
        applicationMetricRegistry.incrementFailUndoUpdatePersonCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.getPersonByKeycloakId(..))")
    public void onSuccessGetPersonByKeycloakId() {
        applicationMetricRegistry.incrementSuccessGetPersonByKeycloakIdCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.getPersonByKeycloakId(..))")
    public void onFailGetPersonByKeycloakId() {
        applicationMetricRegistry.incrementFailGetPersonByKeycloakIdCounter();
    }
}
