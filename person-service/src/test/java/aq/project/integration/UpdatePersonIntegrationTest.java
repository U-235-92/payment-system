package aq.project.integration;

import aq.project.entities.Country;
import aq.project.entities.InstantEmbeddedData;
import aq.project.entities.Person;
import aq.project.exceptions.CountryNotExistsException;
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

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdatePersonIntegrationTest {

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
    public void successfulUpdatePersonTest() throws UserNotExistsException, CountryNotExistsException {
        Country country = countryRepository.findByCountryCode(Stubs.TEST_COUNTRY_CODE).get();
        personService.update(personID, Stubs.getBobPerson(country));
        Person person = personRepository.findById(UUID.fromString(personID)).get();
        assertEquals("Bob", person.getFirstName());
        assertEquals("bob@post.aq", person.getIndividual().getEmail());
        assertEquals(Stubs.TEST_CORRECT_CITY, person.getAddress().getCity());
    }

    @Test
    public void failUpdateUserWithWrongDataTest() {
        assertThrows(ConstraintViolationException.class, () ->
                personService.update(personID, Stubs.getWrongPerson()));
    }

    @Test
    public void failUpdateUserWithUnknownCountryTest() {
        assertThrows(CountryNotExistsException.class, () ->
                personService.update(personID, Stubs.getAlicePerson(Stubs.getUnknownCountry())));
    }

    @Test
    public void failUpdateUnknownUserTest() {
        assertThrows(UserNotExistsException.class, () ->
                personService.update(Stubs.TEST_UNKNOWN_ID, Stubs.getUnknownPerson(getTestCountry())));
    }

    @Test
    public void failUpdatePersonWithIncorrectIdTest() {
        assertThrows(ConstraintViolationException.class, () -> personService.update(Stubs.TEST_INCORRECT_ID, Stubs.getAlicePerson(getTestCountry())));
    }

    private Country getTestCountry() {
        return countryRepository.findByCountryCode(Stubs.TEST_COUNTRY_CODE).get();
    }
}
