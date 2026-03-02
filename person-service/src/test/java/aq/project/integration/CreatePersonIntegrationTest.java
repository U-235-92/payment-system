package aq.project.integration;

import aq.project.entities.*;
import aq.project.exceptions.CountryNotExistsException;
import aq.project.exceptions.UserExistsException;
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

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePersonIntegrationTest {

    @Autowired
    private PersonService personService;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Container
    private static final PostgreSQLContainer POSTGRESQL = Containers.POSTGRESQL;

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
    }

    @AfterEach
    public void deleteEntities() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    public void successfulCreateUserTest() throws UserExistsException, CountryNotExistsException {
        String id = createTestPersonAndGetId(getTestCountry());
        Person alice = personRepository.findById(UUID.fromString(id)).get();
//        Check ids
        assertNotNull(alice);
        assertNotNull(alice.getId());
        assertNotNull(alice.getAddress().getId());
        assertNotNull(alice.getIndividual().getId());
        assertTrue(alice.getAddress().getCountry().getId() > 0);
//        Check basic properties
        assertEquals(Stubs.TEST_CORRECT_FIRST_NAME, alice.getFirstName());
        assertEquals(Stubs.TEST_CORRECT_LAST_NAME, alice.getLastName());
        assertEquals(Stubs.TEST_CORRECT_ADDRESS, alice.getAddress().getAddress());
        assertEquals(Stubs.TEST_CORRECT_STATE, alice.getAddress().getState());
        assertEquals(Stubs.TEST_CORRECT_PHONE, alice.getIndividual().getPhoneNumber());
    }

    @Test
    public void failCreateDuplicateUserTest() throws UserExistsException, CountryNotExistsException {
        createTestPersonAndGetId(getTestCountry());
        assertThrows(UserExistsException.class, () ->
                personService.create(Stubs.getAlicePerson(getTestCountry())));
    }

    @Test
    public void failCreateUserWithWrongDataTest() {
        assertThrows(ConstraintViolationException.class, () ->
                personService.create(Stubs.getWrongPerson()));
    }

    @Test
    public void failCreateUserWithUnknownCountryTest() {
        assertThrows(CountryNotExistsException.class, () ->
                personService.create(Stubs.getAlicePerson(Stubs.getUnknownCountry())));
    }

    private String createTestPersonAndGetId(Country country) throws UserExistsException, CountryNotExistsException {
        return personService.create(Stubs.getAlicePerson(country));
    }

    private Country getTestCountry() {
        return countryRepository.findByCountryCode(Stubs.TEST_COUNTRY_CODE).get();
    }
}
