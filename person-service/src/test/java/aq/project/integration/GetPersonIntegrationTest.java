package aq.project.integration;

import aq.project.entities.Country;
import aq.project.entities.InstantEmbeddedData;
import aq.project.entities.Person;
import aq.project.exceptions.UserNotExistsException;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.services.PersonService;
import aq.project.util.Containers;
import aq.project.util.PostgresqlTestApplicationProperties;
import aq.project.util.Stubs;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DirtiesContext
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
    private String personID;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresqlTestApplicationProperties.configureProperties(registry, POSTGRESQL);
    }

    @BeforeEach
    public void saveEntities() {
        Country country = new Country();
        country.setCode(Stubs.TEST_COUNTRY_CODE);
        country.setName(Stubs.TEST_COUNTRY_NAME);
        country.setInstantEmbeddedData(new InstantEmbeddedData());
        countryRepository.save(country);
        personID = personRepository.save(Stubs.getAlicePerson(country)).getId().toString();
    }

    @AfterEach
    public void deleteEntities() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    public void successfulGetUserByEmailTest() throws UserNotExistsException {
        Person person = personService.getByEmail(Stubs.TEST_CORRECT_EMAIL);
        assertNotNull(person);
    }

    @Test
    public void failGetUserByNotExistsEmailTest() {
        assertThrows(UserNotExistsException.class, () -> personService.getByEmail(Stubs.TEST_UNKNOWN_EMAIL));
    }

    @Test
    public void failGetUserByIncorrectEmailTest() {
        assertThrows(ConstraintViolationException.class, () -> personService.getByEmail(Stubs.TEST_INCORRECT_EMAIL));
    }

    @Test
    public void successfulGetUserByIdTest() throws UserNotExistsException {
        Person person = personService.getById(personID);
        assertNotNull(person);
    }

    @Test
    public void failGetUserByNotExistsIdTest() {
        assertThrows(UserNotExistsException.class, () -> personService.getById(Stubs.TEST_UNKNOWN_ID));
    }

    @Test
    public void failGetUserByIncorrectIdTest() {
        assertThrows(ConstraintViolationException.class, () -> personService.getById(Stubs.TEST_INCORRECT_ID));
    }
}
