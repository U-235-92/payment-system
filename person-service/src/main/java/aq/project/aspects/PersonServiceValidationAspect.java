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

    @Before("execution(* aq.project.services.PersonService.getById(..)) && args(id)")
    public void checkGetById(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String id) {}

    @Before("execution(* aq.project.services.PersonService.update(..)) && args(id, from)")
    public void checkUpdate(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String id, @Valid Person from) {}

    @Before("execution(* aq.project.services.PersonService.delete(..)) && args(id)")
    public void checkDelete(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String id) {}
}
