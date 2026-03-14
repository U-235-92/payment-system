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
public class PersonServiceMetricsAspect {

    private final ApplicationMetricRegistry applicationMetricRegistry;

    @After("execution(* aq.project.controllers.PersonRestController.create(..))")
    public void onSuccessPersonCreate() {
        applicationMetricRegistry.incrementSuccessCreatePersonCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.create(..))")
    public void onFailPersonCreate() {
        applicationMetricRegistry.incrementFailCreatePersonCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.deleteByPersonId(..))")
    public void onSuccessPersonDeleteByPersonId() {
        applicationMetricRegistry.incrementSuccessDeletePersonByPersonIdCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.deleteByPersonId(..))")
    public void onFailPersonDeleteByPersonId() {
        applicationMetricRegistry.incrementFailDeletePersonByPersonIdCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.deleteByKeycloakId(..))")
    public void onSuccessPersonDeleteByKeycloakId() {
        applicationMetricRegistry.incrementSuccessDeletePersonByKeycloakIdCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.deleteByKeycloakId(..))")
    public void onFailPersonDeleteByKeycloakId() {
        applicationMetricRegistry.incrementFailDeletePersonByKeycloakIdCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.update(..))")
    public void onSuccessPersonUpdate() {
        applicationMetricRegistry.incrementSuccessUpdatePersonCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.update(..))")
    public void onFailPersonUpdate() {
        applicationMetricRegistry.incrementFailUpdatePersonCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.getByKeycloakId(..))")
    public void onSuccessReadKeycloakById() {
        applicationMetricRegistry.incrementSuccessReadPersonByKeycloakIdCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.getByKeycloakId(..))")
    public void onFailReadKeycloakById() {
        applicationMetricRegistry.incrementFailReadPersonByKeycloakIdCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.getByPersonId(..))")
    public void onSuccessReadPersonById() {
        applicationMetricRegistry.incrementSuccessReadPersonByPersonIdCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.getByPersonId(..))")
    public void onFailReadPersonById() {
        applicationMetricRegistry.incrementFailReadPersonByPersonIdCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.getByEmail(..))")
    public void onSuccessReadPersonByEmail() {
        applicationMetricRegistry.incrementSuccessReadPersonByEmailCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.getByEmail(..))")
    public void onFailReadPersonByEmail() {
        applicationMetricRegistry.incrementFailReadPersonByEmailCounter();
    }
}
