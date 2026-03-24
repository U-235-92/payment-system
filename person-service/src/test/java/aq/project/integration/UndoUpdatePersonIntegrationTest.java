package aq.project.integration;

import aq.project.controllers.PersonRestController;
import aq.project.entities.Person;
import aq.project.entities.UndoEvent;
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

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UndoUpdatePersonIntegrationTest {

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
        personRestController.createPerson(DTO.getValidCreateIndividualDataDTO());
    }

    @AfterEach
    public void tearDown() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    public void successUndoUpdatePersonTest() throws Exception {
        personRestController.updatePerson(DTO.getUpdateIndividualDataDTO());
        personRestController.undoUpdatePerson(DTO.getValidUndoUpdateOperationDTO());
        Optional<Person> person = personRepository.findByKeycloakId(Constants.CORRECT_PERSON_KEYCLOAK_ID);
        Assertions.assertNotNull(person.get());
        Assertions.assertEquals(DTO.getValidCreateIndividualDataDTO().getKeycloakUserId(), person.get().getKeycloakId());
        Assertions.assertEquals(DTO.getValidCreateIndividualDataDTO().getFirstName(), person.get().getFirstName());
        Assertions.assertEquals(DTO.getValidCreateIndividualDataDTO().getLastName(), person.get().getLastName());
    }

    @Test
    public void failCallUndoUpdatePersonAfterCallUndoUpdatePersonTest() throws Exception {
        personRestController.updatePerson(DTO.getUpdateIndividualDataDTO());
//        First [undo-update] call
        personRestController.undoUpdatePerson(DTO.getValidUndoUpdateOperationDTO());
//        Second [undo-update] call
        Assertions.assertThrows(NotExpectedUndoOperationCallException.class,
                () -> personRestController.undoUpdatePerson(DTO.getValidUndoUpdateOperationDTO()));
    }

    @Test
    public void failCallUndoUpdatePersonWhenPreviousCallWasNotUpdateTest() throws Exception {
        personRestController.deletePersonByKeycloakId(Constants.CORRECT_PERSON_KEYCLOAK_ID);
        Assertions.assertThrows(NotExpectedUndoOperationCallException.class,
                () -> personRestController.undoUpdatePerson(DTO.getValidUndoUpdateOperationDTO()));
    }

    @Test
    public void failCallUndoUpdatePersonWhenPreviousCallWasCreatePersonTest() {
        Assertions.assertThrows(NotExpectedUndoOperationCallException.class,
                () -> personRestController.undoUpdatePerson(DTO.getValidUndoUpdateOperationDTO()));
    }

    @Test
    public void failCallUndoUpdatePersonWithWrongUndoEvent() {
        UndoEvent invalidUndoEvent = Events.getInvalidUndoEvent();
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.undoUpdatePerson(invalidUndoEvent));
    }

    @Test
    @Disabled("To use this test you have to disable @BeforeEach because before run this one database MUST be clean")
    public void failCallUndoUpdatePersonWhenDatabaseEmptyTest() {
        Assertions.assertThrows(NotFoundUndoOperationCallException.class,
                () -> personRestController.undoUpdatePerson(DTO.getValidUndoUpdateOperationDTO()));
    }
}
