package aq.project.integration;

import aq.project.entities.Country;
import aq.project.entities.InstantEmbeddedData;
import aq.project.entities.Person;
import aq.project.exceptions.UserNotExistsException;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.services.PersonService;
import aq.project.util.containers.Containers;
import aq.project.util.configs.PostgresqlTestApplicationProperties;
import aq.project.util.entity.Persons;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static aq.project.util.entity.Constants.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetPersonIntegrationTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Container
    private static final PostgreSQLContainer POSTGRESQL = Containers.POSTGRESQL;

    private Person alice;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresqlTestApplicationProperties.configureProperties(registry, POSTGRESQL);
    }

    @BeforeEach
    public void saveEntities() {
        Country country = new Country();
        country.setCode(COUNTRY_CODE);
        country.setName(COUNTRY_NAME);
        country.setInstantEmbeddedData(new InstantEmbeddedData());
        countryRepository.save(country);
        alice = Persons.getAlicePerson(country);
        UUID personID = personRepository.save(alice).getId();
        alice.setId(personID);
    }

    @AfterEach
    public void deleteEntities() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    public void successfulGetUserByEmailTest() throws UserNotExistsException {
        Person person = personService.getByEmail(CORRECT_EMAIL);
        assertNotNull(person);
    }

    @Test
    public void failGetUserByNotExistsEmailTest() {
        assertThrows(UserNotExistsException.class, () -> personService.getByEmail(UNKNOWN_EMAIL));
    }

    @Test
    public void failGetUserByIncorrectEmailTest() {
        assertThrows(ConstraintViolationException.class, () -> personService.getByEmail(INCORRECT_EMAIL));
    }

    @Test
    public void successfulGetUserByKeycloakIdTest() throws UserNotExistsException {
        Person person = personService.getByKeycloakId(alice.getKeycloakId());
        assertNotNull(person);
    }

    @Test
    public void failGetUserByNotExistsKeycloakIdTest() {
        assertThrows(UserNotExistsException.class, () -> personService.getByKeycloakId(UNKNOWN_PERSON_ID));
    }

    @Test
    public void failGetUserByIncorrectKeycloakIdTest() {
        assertThrows(ConstraintViolationException.class, () -> personService.getByKeycloakId(INCORRECT_PERSON_ID));
    }

    @Test
    public void successfulGetUserByPersonIdTest() throws UserNotExistsException {
        Person person = personService.getByPersonId(alice.getId().toString());
        assertNotNull(person);
    }

    @Test
    public void failGetUserByNotExistsPersonIdTest() {
        assertThrows(UserNotExistsException.class, () -> personService.getByPersonId(UNKNOWN_PERSON_ID));
    }

    @Test
    public void failGetUserByIncorrectPersonIdTest() {
        assertThrows(ConstraintViolationException.class, () -> personService.getByPersonId(INCORRECT_PERSON_ID));
    }
}
