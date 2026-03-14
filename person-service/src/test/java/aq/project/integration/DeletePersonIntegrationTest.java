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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static aq.project.util.entity.Constants.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeletePersonIntegrationTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Container
    private static final PostgreSQLContainer POSTGRESQL = Containers.POSTGRESQL;

    private UUID personID;

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
        personID = personRepository.save(alice).getId();
        alice.setId(personID);
    }

    @AfterEach
    public void deleteEntities() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    public void successfulDeletePersonByPersonIdTest() throws UserNotExistsException {
         personService.deleteByPersonId(personID.toString());
         assertTrue(personRepository.findById(personID).isEmpty());
    }

    @Test
    public void failDeleteNotExistsPersonByPersonIdTest() {
        assertThrows(UserNotExistsException.class, () -> personService.deleteByPersonId(UNKNOWN_PERSON_ID));
    }

    @Test
    public void failDeleteIncorrectPersonByPersonIdTest() {
        assertThrows(ConstraintViolationException.class, () -> personService.deleteByPersonId(INCORRECT_PERSON_ID));
    }

    @Test
    public void successfulDeletePersonByKeycloakIdTest() throws UserNotExistsException {
        personService.deleteByKeycloakId(alice.getKeycloakId());
        assertTrue(personRepository.findByKeycloakId(alice.getKeycloakId()).isEmpty());
    }

    @Test
    public void failDeleteNotExistsPersonByKeycloakIdTest() {
        assertThrows(UserNotExistsException.class, () -> personService.deleteByKeycloakId(UNKNOWN_PERSON_ID));
    }

    @Test
    public void failDeleteIncorrectPersonByKeycloakIdTest() {
        assertThrows(ConstraintViolationException.class, () -> personService.deleteByKeycloakId(INCORRECT_PERSON_ID));
    }
}
