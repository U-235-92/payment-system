package aq.project.test.services.person.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.controllers.PersonRestController;
import aq.project.dto.PersonServiceCreatePersonRequestDto;
import aq.project.dto.PersonServiceUndoUpdatePersonRequestDto;
import aq.project.dto.PersonServiceUpdatePersonRequestDto;
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
import java.util.UUID;

import static aq.project._utils.entities.PersonServiceEntities.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UndoUpdatePersonIntegrationTest {

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
        registry.add("resilience4j.ratelimiter.update-person-rate-limiter.limit-for-period", () -> 100);
    }

    @AfterEach
    public void cleanUpRepositories() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
        serviceOperationRepository.deleteAll();
    }

    @Test
    public void successUndoUpdatePerson() {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String email = "test@mail.aq";

        String createCountryCode = "ATA";
        String createCountryName = "Antarctica";

        String createPersonFirstName = "CreatedFirstName";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);
        personServiceCreatePersonRequestDto.setFirstName(createPersonFirstName);
        personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

        personRestController.createPerson(traceId, personServiceCreatePersonRequestDto, authorization);

        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getValidPersonServiceUpdatePersonRequestDto();
        personServiceUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceUpdatePersonRequestDto.setFirstName("UpdatedFirstName");

        personRestController.updatePerson(traceId, personServiceUpdatePersonRequestDto, authorization);

        PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto = getValidPersonServiceUndoUpdatePersonRequestDto();
        personServiceUndoUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personRestController
                .undoUpdatePerson(traceId, personServiceUndoUpdatePersonRequestDto, authorization));

        Person person = personRepository.findByKeycloakId(personKeycloakId.toString()).get();

        Assertions.assertEquals(createPersonFirstName, person.getFirstName());
    }

    @Test
    public void failUndoUpdatePersonOnInvalidUndoUpdateRequestDto() {
//        Arrange
        PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto = getInvalidPersonServiceUndoUpdatePersonRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.undoUpdatePerson(personServiceUndoUpdatePersonRequestDto));
    }

    @Test
    public void failUndoUpdatePersonOnNullUndoUpdateRequestDto() {
//        Arrange
        PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.undoUpdatePerson(personServiceUndoUpdatePersonRequestDto));
    }

    @Test
    public void failUndoUpdatePersonOnFallbackCall() {
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

            PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getValidPersonServiceUpdatePersonRequestDto();
            personServiceUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
            personServiceUpdatePersonRequestDto.setFirstName("UpdatedFirstName");

            personRestController.updatePerson(traceId, personServiceUpdatePersonRequestDto, authorization);
        }

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            UUID personKeycloakId = personKeycloakIdList.get(i);

            PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto = getValidPersonServiceUndoUpdatePersonRequestDto();
            personServiceUndoUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

            if(i < RATE_LIMIT - 1)
                new Thread(() -> personService.undoUpdatePerson(personServiceUndoUpdatePersonRequestDto)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> personService.undoUpdatePerson(personServiceUndoUpdatePersonRequestDto));
        }
    }
}
