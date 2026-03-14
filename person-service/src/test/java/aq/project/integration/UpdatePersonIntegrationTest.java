package aq.project.integration;

import aq.project.entities.Country;
import aq.project.entities.InstantEmbeddedData;
import aq.project.entities.Person;
import aq.project.exceptions.CountryNotExistsException;
import aq.project.exceptions.UserNotExistsException;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.services.PersonService;
import aq.project.util.containers.Containers;
import aq.project.util.entity.Countries;
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
        country.setCode(COUNTRY_CODE);
        country.setName(COUNTRY_NAME);
        country.setInstantEmbeddedData(new InstantEmbeddedData());
        countryRepository.save(country);
    }

    @AfterEach
    public void deleteEntities() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    public void successfulUpdatePersonTest() throws UserNotExistsException, CountryNotExistsException {
//        Prepare
        Country country = countryRepository.findByCountryCode(COUNTRY_CODE).get();
        Person originalPerson = Persons.getAlicePerson(country);
        originalPerson.setId(personRepository.save(originalPerson).getId());
        originalPerson.setFirstName("Bob");
        originalPerson.getIndividual().setEmail("bob@post.aq");
//        Run test logic
        personService.update(originalPerson);
//        Check results
        Person updatedPerson = personRepository.findByKeycloakId(originalPerson.getKeycloakId()).get();
        assertEquals("Bob", updatedPerson.getFirstName());
        assertEquals("bob@post.aq", updatedPerson.getIndividual().getEmail());
        assertEquals(CORRECT_CITY, updatedPerson.getAddress().getCity());
    }

    @Test
    public void failUpdateUserWithWrongDataTest() {
        assertThrows(ConstraintViolationException.class, () ->
                personService.update(Persons.getWrongPerson()));
    }

    @Test
    public void failUpdateUserWithUnknownCountryTest() {
        assertThrows(CountryNotExistsException.class, () ->
                personService.update(Persons.getAlicePerson(Countries.getUnknownCountry())));
    }

    @Test
    public void failUpdateUnknownUserTest() {
        assertThrows(UserNotExistsException.class, () ->
                personService.update(Persons.getUnknownPerson(getTestCountry())));
    }

    @Test
    public void failUpdatePersonWithIncorrectIdTest() {
        assertThrows(IllegalArgumentException.class, () ->
                personService.update(Persons.getPersonWithIncorrectId(getTestCountry())));
    }

    private Country getTestCountry() {
        return countryRepository.findByCountryCode(COUNTRY_CODE).get();
    }
}
