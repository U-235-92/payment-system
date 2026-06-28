package aq.project.integration;

import aq.project.controllers.PersonRestController;
import aq.project.entities.Person;
import aq.project.entities.UndoOperation;
import aq.project.exceptions.CountryNotExistsException;
import aq.project.exceptions.NotExpectedUndoOperationCallException;
import aq.project.exceptions.NotFoundUndoOperationCallException;
import aq.project.exceptions.UserExistsException;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.services.PersonService;
import aq.project.util.configs.PostgresqlTestApplicationProperties;
import aq.project.util.containers.Containers;
import aq.project.util.entity.Constants;
import aq.project.util.entity.Countries;
import aq.project.util.entity.DTO;
import aq.project.util.entity.Events;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
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
import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UndoDeletePersonIntegrationTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @Container
    private static final PostgreSQLContainer POSTGRESQL = Containers.POSTGRESQL;

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRestController personRestController;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgresqlTestApplicationProperties.configureProperties(registry, POSTGRESQL);
    }

    @BeforeEach
    public void setUp() throws UserExistsException, CountryNotExistsException {
        countryRepository.save(Countries.getValidTestCountry());
        personRestController.createPerson(TRACE_ID, DTO.getValidCreateIndividualDataDTO());
    }

    @AfterEach
    public void tearDown() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    public void successUndoDeletePersonTest() throws Exception {
        personRestController.deletePersonByKeycloakId(Constants.CORRECT_PERSON_KEYCLOAK_ID, TRACE_ID);
        personRestController.undoDeletePerson(TRACE_ID, DTO.getValidUndoDeleteOperationDTO());
        Optional<Person> person = personRepository.findByKeycloakId(Constants.CORRECT_PERSON_KEYCLOAK_ID);
        Assertions.assertNotNull(person.get());
        Assertions.assertEquals(Constants.CORRECT_PERSON_KEYCLOAK_ID, person.get().getKeycloakId());
    }

    @Test
    public void failCallUndoDeletePersonAfterCallUndoDeletePersonTest() throws Exception {
        personRestController.deletePersonByKeycloakId(Constants.CORRECT_PERSON_KEYCLOAK_ID, TRACE_ID);
//        First [undo-delete] call
        personRestController.undoDeletePerson(TRACE_ID, DTO.getValidUndoDeleteOperationDTO());
//        Second [undo-delete] call
        Assertions.assertThrows(NotExpectedUndoOperationCallException.class,
                () -> personRestController.undoDeletePerson(TRACE_ID, DTO.getValidUndoDeleteOperationDTO()));
    }

    @Test
    public void failCallUndoDeletePersonWhenPreviousCallWasNotDeleteTest() throws Exception {
        personRestController.updatePerson(TRACE_ID, DTO.getUpdateIndividualDataDTO());
        Assertions.assertThrows(NotExpectedUndoOperationCallException.class,
                () -> personRestController.undoDeletePerson(TRACE_ID, DTO.getValidUndoDeleteOperationDTO()));
    }

    @Test
    public void failCallUndoDeletePersonWhenPreviousCallWasCreatePersonTest() {
        Assertions.assertThrows(NotExpectedUndoOperationCallException.class,
                () -> personRestController.undoDeletePerson(TRACE_ID, DTO.getValidUndoDeleteOperationDTO()));
    }

    @Test
    public void failCallUndoDeletePersonWithWrongUndoOperation() {
        UndoOperation invalidUndoOperation = Events.getInvalidUndoOperation();
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.undoDeletePerson(invalidUndoOperation));
    }

    @Test
    @Disabled("To use this test you have to disable @BeforeEach because before run this one database MUST be clean")
    public void failCallUndoDeletePersonWhenDatabaseEmptyTest() {
        Assertions.assertThrows(NotFoundUndoOperationCallException.class,
                () -> personRestController.undoDeletePerson(TRACE_ID, DTO.getValidUndoDeleteOperationDTO()));
    }
}
