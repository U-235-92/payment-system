package aq.project.aspects;

import aq.project.entities.Person;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Aspect
@Validated
@Component
public class PersonServiceValidationAspect {

    @Before("execution(* aq.project.services.PersonService.createPerson(..)) && args(person)")
    public void checkCreatePerson(@Valid Person person) {}

    @Before("execution(* aq.project.services.PersonService.getPersonByKeycloakId(..)) && args(keycloakId)")
    public void checkGetPersonByKeycloakId(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {}

    @Before("execution(* aq.project.services.PersonService.updatePerson(..)) && args(from)")
    public void checkUpdatePerson(@Valid Person from) {}

    @Before("execution(* aq.project.services.PersonService.undoUpdatePerson(..)) && args(keycloakId)")
    public void checkUndoUpdatePerson(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {}

    @Before("execution(* aq.project.services.PersonService.deletePersonByKeycloakId(..)) && args(keycloakId)")
    public void checkDeletePersonByKeycloakId(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {}

    @Before("execution(* aq.project.services.PersonService.undoDeletePersonByKeycloakId(..)) && args(keycloakId)")
    public void checkUndoDeletePersonByPersonId(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {}
}
