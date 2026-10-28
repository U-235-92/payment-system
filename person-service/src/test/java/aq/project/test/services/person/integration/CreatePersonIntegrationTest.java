package aq.project.test.services.person.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PersonServiceCreatePersonRequestDto;
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

import java.util.UUID;

import static aq.project._utils.entities.PersonServiceEntities.getInvalidPersonServiceCreatePersonRequestDto;
import static aq.project._utils.entities.PersonServiceEntities.getValidPersonServiceCreatePersonRequestDto;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePersonIntegrationTest {

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

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    public void cleanUpRepositories() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
        serviceOperationRepository.deleteAll();
    }

    @Test
    public void successCreatePerson() {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String email = "test@mail.aq";
        String countryCode = "ATA";
        String countryName = "Antarctica";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);

        personServiceCreatePersonRequestDto.getCountry().setName(countryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(countryCode);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService.createPerson(personServiceCreatePersonRequestDto));
        Assertions.assertNotNull(personRepository.findByKeycloakId(personKeycloakId.toString()).get());
    }

    @Test
    public void failCreatePersonOnInvalidPersonServiceCreatePersonRequestDto() {
//        Arrange
        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getInvalidPersonServiceCreatePersonRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.createPerson(personServiceCreatePersonRequestDto));
    }

    @Test
    public void failCreatePersonOnNullPersonServiceCreatePersonRequestDto() {
//        Arrange
        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.createPerson(personServiceCreatePersonRequestDto));
    }

    @Test
    public void failCreatePersonOnFallbackCall() {
//        Arrange
        final int RATE_LIMIT = 50;

        for(int i = 0; i < RATE_LIMIT; i++) {
            String email = String.format("test%d@mail.aq", i);
            String countryCode = "ATA";
            String countryName = "Antarctica";

            PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
            personServiceCreatePersonRequestDto.setPersonKeycloakId(UUID.randomUUID());
            personServiceCreatePersonRequestDto.setEmail(email);

            personServiceCreatePersonRequestDto.getCountry().setName(countryName);
            personServiceCreatePersonRequestDto.getCountry().setCode(countryCode);

//        Act & Assert
            if(i < RATE_LIMIT - 1)
                new Thread(() -> personService.createPerson(personServiceCreatePersonRequestDto)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> personService.createPerson(personServiceCreatePersonRequestDto));
        }
    }
}
