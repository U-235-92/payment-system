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

    @After("execution(* aq.project.controllers.PersonRestController.delete(..))")
    public void onSuccessPersonDelete() {
        applicationMetricRegistry.incrementSuccessDeletePersonCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.delete(..))")
    public void onFailPersonDelete() {
        applicationMetricRegistry.incrementFailDeletePersonCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.update(..))")
    public void onSuccessPersonUpdate() {
        applicationMetricRegistry.incrementSuccessUpdatePersonCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.update(..))")
    public void onFailPersonUpdate() {
        applicationMetricRegistry.incrementFailUpdatePersonCounter();
    }

    @After("execution(* aq.project.controllers.PersonRestController.getById(..))")
    public void onSuccessReadPersonById() {
        applicationMetricRegistry.incrementSuccessReadPersonByIdCounter();
    }

    @AfterThrowing("execution(* aq.project.controllers.PersonRestController.getById(..))")
    public void onFailReadPersonById() {
        applicationMetricRegistry.incrementFailReadPersonByIdCounter();
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
