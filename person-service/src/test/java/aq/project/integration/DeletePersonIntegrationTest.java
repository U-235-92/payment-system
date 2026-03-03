package aq.project.integration;

import aq.project.entities.Country;
import aq.project.entities.InstantEmbeddedData;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DirtiesContext
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
    public void successfulDeletePersonTest() throws UserNotExistsException {
         personService.delete(personID);
         assertTrue(personRepository.findById(UUID.fromString(personID)).isEmpty());
    }

    @Test
    public void failDeleteNotExistsPersonTest() {
        assertThrows(UserNotExistsException.class, () -> personService.delete(Stubs.TEST_UNKNOWN_ID));
    }

    @Test
    public void failDeleteIncorrectPersonIdTest() {
        assertThrows(ConstraintViolationException.class, () -> personService.delete(Stubs.TEST_INCORRECT_ID));
    }
}
