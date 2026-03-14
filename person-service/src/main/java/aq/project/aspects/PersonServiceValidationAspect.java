package aq.project.aspects;

import aq.project.entities.Person;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Aspect
@Validated
@Component
public class PersonServiceValidationAspect {

    @Before("execution(* aq.project.services.PersonService.create(..)) && args(person)")
    public void checkCreate(@Valid Person person) {}

    @Before("execution(* aq.project.services.PersonService.getByEmail(..)) && args(email)")
    public void checkGetByEmail(@Email String email) {}

    @Before("execution(* aq.project.services.PersonService.getByKeycloakId(..)) && args(keycloakId)")
    public void checkGetByKeycloakId(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {}

    @Before("execution(* aq.project.services.PersonService.getByPersonId(..)) && args(personId)")
    public void checkGetByPersonId(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String personId) {}

    @Before("execution(* aq.project.services.PersonService.update(..)) && args(from)")
    public void checkUpdate(@Valid Person from) {}

    @Before("execution(* aq.project.services.PersonService.deleteByPersonId(..)) && args(personId)")
    public void checkDeleteByPersonId(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String personId) {}

    @Before("execution(* aq.project.services.PersonService.deleteByKeycloakId(..)) && args(keycloakId)")
    public void checkDeleteByKeycloakId(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {}
}
