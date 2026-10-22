package aq.project.test.services.person.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.controllers.PersonRestController;
import aq.project.dto.PersonServiceCreatePersonRequestDto;
import aq.project.dto.PersonServiceUndoDeletePersonRequestDto;
import aq.project.entities.Person;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.repositories.ServiceOperationRepository;
import aq.project.services.PersonService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.entities.PersonServiceEntities.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UndoDeletePersonIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private ServiceOperationRepository serviceOperationRepository;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRestController personRestController;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("resilience4j.ratelimiter.create-person-rate-limiter.limit-for-period", () -> 100);
        registry.add("resilience4j.ratelimiter.delete-person-rate-limiter.limit-for-period", () -> 100);
    }

    @AfterEach
    public void cleanUpRepositories() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
        serviceOperationRepository.deleteAll();
    }

    @Test
    public void successUndoDeletePerson() {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String email = "test@mail.aq";

        String createCountryCode = "ATA";
        String createCountryName = "Antarctica";

        String createFirstName = "CreateFirstName";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);
        personServiceCreatePersonRequestDto.setFirstName(createFirstName);
        personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

        personRestController.createPerson(traceId, personServiceCreatePersonRequestDto, authorization);

        personRestController.deletePerson(personKeycloakId, traceId, authorization);

        PersonServiceUndoDeletePersonRequestDto personServiceUndoDeletePersonRequestDto = getValidPersonServiceUndoDeletePersonRequestDto();
        personServiceUndoDeletePersonRequestDto.setPersonKeycloakId(personKeycloakId);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personRestController
                .undoDeletePerson(traceId, personServiceUndoDeletePersonRequestDto, authorization));

        Optional<Person> personOptional = personRepository.findByKeycloakId(personKeycloakId.toString());

        Assertions.assertTrue(personOptional.isPresent());
        Assertions.assertEquals(createCountryCode, personOptional.get().getCountry().getCode());
        Assertions.assertEquals(createCountryName, personOptional.get().getCountry().getName());
        Assertions.assertEquals(createFirstName, personOptional.get().getFirstName());

    }

    @Test
    public void failUndoDeletePersonOnInvalidUndoDeleteRequestDto() {
//        Arrange
        PersonServiceUndoDeletePersonRequestDto personServiceUndoDeletePersonRequestDto = getInvalidPersonServiceUndoDeletePersonRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.undoDeletePerson(personServiceUndoDeletePersonRequestDto));
    }

    @Test
    public void failUndoDeletePersonOnNullUndoDeleteRequestDto() {
//        Arrange
        PersonServiceUndoDeletePersonRequestDto personServiceUndoDeletePersonRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.undoDeletePerson(personServiceUndoDeletePersonRequestDto));
    }

    @Test
    public void failUndoDeletePersonOnFallbackCall() {
//        Arrange
        final int RATE_LIMIT = 50;

        List<UUID> personKeycloakIdList = new ArrayList<>();

        for(int i = 0; i < RATE_LIMIT; i++) {
            UUID personKeycloakId = UUID.randomUUID();

            String traceId = UUID.randomUUID().toString();
            String authorization = UUID.randomUUID().toString();

            String email = String.format("test%d@mail.aq", i);

            String createCountryCode = "ATA";
            String createCountryName = "Antarctica";

            String createFirstName = "CreateFirstName";

            PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
            personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
            personServiceCreatePersonRequestDto.setEmail(email);
            personServiceCreatePersonRequestDto.setFirstName(createFirstName);
            personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
            personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

            personRestController.createPerson(traceId, personServiceCreatePersonRequestDto, authorization);

            personKeycloakIdList.add(personKeycloakId);

            personRestController.deletePerson(personKeycloakId, traceId, authorization);
        }

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            UUID personKeycloakId = personKeycloakIdList.get(i);

            PersonServiceUndoDeletePersonRequestDto undoDeletePersonRequestDto = getInvalidPersonServiceUndoDeletePersonRequestDto();
            undoDeletePersonRequestDto.setPersonKeycloakId(personKeycloakId);

            if(i < RATE_LIMIT - 1)
                new Thread(() -> personService.undoDeletePerson(undoDeletePersonRequestDto)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> personService.undoDeletePerson(undoDeletePersonRequestDto));
        }
    }
}
