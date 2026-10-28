package aq.project.test.services.person.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PersonServiceCreatePersonRequestDto;
import aq.project.dto.PersonServiceUpdatePersonRequestDto;
import aq.project.entities.Country;
import aq.project.entities.Person;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.repositories.ServiceOperationRepository;
import aq.project.services.PersonService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static aq.project._utils.entities.PersonServiceEntities.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdatePersonIntegrationTest {

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
        registry.add("resilience4j.ratelimiter.create-person-rate-limiter.limit-for-period", () -> 100);
    }

    @BeforeEach
    public void cleanUpRepositories() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
        serviceOperationRepository.deleteAll();
    }

    @Test
    public void successUpdatePerson() {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String email = "test@mail.aq";

        String createCountryCode = "ATA";
        String createCountryName = "Antarctica";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);
        personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

        personService.createPerson(personServiceCreatePersonRequestDto);

        String updateCountryCode = "AUS";
        String updateCountryName = "Australia";
        String updateFirstName = "TestFirstName";
        String updateAddress = "TestStreet";

        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getValidPersonServiceUpdatePersonRequestDto();
        personServiceUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceUpdatePersonRequestDto.getCountry().setName(updateCountryName);
        personServiceUpdatePersonRequestDto.getCountry().setCode(updateCountryCode);
        personServiceUpdatePersonRequestDto.setFirstName(updateFirstName);
        personServiceUpdatePersonRequestDto.getAddress().setAddress(updateAddress);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService.updatePerson(personServiceUpdatePersonRequestDto));

        Person person = personRepository.findByKeycloakId(personKeycloakId.toString()).get();

        Assertions.assertEquals(updateCountryCode, person.getCountry().getCode());
        Assertions.assertEquals(updateCountryName, person.getCountry().getName());
        Assertions.assertEquals(updateFirstName, person.getFirstName());
        Assertions.assertEquals(updateAddress, person.getAddress().getAddress());
        Assertions.assertEquals(email, person.getIndividual().getEmail());
    }

    @Test
    public void failUpdatePersonOnInvalidPersonServiceUpdatePersonRequestDto() {
//        Arrange
        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getInvalidPersonServiceUpdatePersonRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.updatePerson(personServiceUpdatePersonRequestDto));
    }

    @Test
    public void failUpdatePersonOnNullPersonServiceUpdatePersonRequestDto() {
//        Arrange
        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> personService.updatePerson(personServiceUpdatePersonRequestDto));
    }

    @Test
    public void failUpdatePersonOnFallbackCall() {
//        Arrange
        final int RATE_LIMIT = 50;

        List<UUID> personKeycloakIdList = new ArrayList<>();

        for(int i = 0; i < RATE_LIMIT; i++) {
            UUID personKeycloakId = UUID.randomUUID();

            String email = String.format("test%d@mail.aq", i);

            String createCountryCode = "ATA";
            String createCountryName = "Antarctica";

            Country createCountry = getValidCountry();
            createCountry.setCode(createCountryCode);
            createCountry.setName(createCountryName);

            PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
            personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
            personServiceCreatePersonRequestDto.setEmail(email);
            personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
            personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

            personService.createPerson(personServiceCreatePersonRequestDto);

            personKeycloakIdList.add(personKeycloakId);
        }

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            UUID personKeycloakId = personKeycloakIdList.get(i);

            String updateCountryCode = "AUS";
            String updateCountryName = "Australia";
            String updateFirstName = "TestFirstName";
            String updateAddress = "TestStreet";

            PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getValidPersonServiceUpdatePersonRequestDto();
            personServiceUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
            personServiceUpdatePersonRequestDto.getCountry().setName(updateCountryName);
            personServiceUpdatePersonRequestDto.getCountry().setCode(updateCountryCode);
            personServiceUpdatePersonRequestDto.setFirstName(updateFirstName);
            personServiceUpdatePersonRequestDto.getAddress().setAddress(updateAddress);

            if(i < RATE_LIMIT - 1)
                new Thread(() -> personService.updatePerson(personServiceUpdatePersonRequestDto)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> personService.updatePerson(personServiceUpdatePersonRequestDto));
        }
    }
}
