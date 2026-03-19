package aq.project.integration;

import aq.project.controllers.PersonRestController;
import aq.project.entities.Person;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.util.configs.PostgresqlTestApplicationProperties;
import aq.project.util.containers.Containers;
import aq.project.util.entity.Countries;
import aq.project.util.entity.DTO;
import org.junit.jupiter.api.Assertions;
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

import java.util.Optional;

import static aq.project.util.entity.Constants.CORRECT_PERSON_KEYCLOAK_ID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UndoDeletePersonIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL = Containers.POSTGRESQL;

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private PersonRestController personRestController;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresqlTestApplicationProperties.configureProperties(registry, POSTGRESQL);
    }

    @Test
    public void successUndoDeletePersonTest() throws Exception {
        countryRepository.save(Countries.getValidTestCountry());
        personRestController.createPerson(DTO.getValidCreateIndividualDataEvent());
        personRestController.deletePersonByKeycloakId(CORRECT_PERSON_KEYCLOAK_ID);
        personRestController.undoDeletePersonByKeycloakId(DTO.getValidUndoDeleteOperationDTO());
        Optional<Person> person = personRepository.findByKeycloakId(CORRECT_PERSON_KEYCLOAK_ID);
        Assertions.assertNotNull(person.get());
        Assertions.assertEquals(CORRECT_PERSON_KEYCLOAK_ID, person.get().getKeycloakId());
    }
}
