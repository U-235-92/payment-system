package aq.project.test.controllers;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.controllers.PersonRestController;
import aq.project.dto.PersonServiceCreatePersonRequestDto;
import aq.project.dto.PersonServiceUpdatePersonRequestDto;
import aq.project.entities.Country;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.repositories.ServiceOperationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static aq.project._utils.entities.PersonServiceEntities.*;
import static aq.project.controller.PersonRestControllerApi.PATH_UPDATE_PERSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdatePersonWebTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    private PersonRestController personRestController;

    @Autowired
    private MockMvc mockMvc;

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
    @WithMockUser(username = "test-user", password = "test-password")
    public void successUpdatePerson() throws Exception {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String email = "test@mail.aq";

        String createCountryCode = "ATA";
        String createCountryName = "Antarctica";

        Country createCountry = getValidCountry();
        createCountry.setCode(createCountryCode);
        createCountry.setName(createCountryName);

        String updateCountryCode = "AUS";
        String updateCountryName = "Australia";
        String updateFirstName = "TestFirstName";
        String updateAddress = "TestStreet";

        Country updateCountry = getValidCountry();
        updateCountry.setCode(updateCountryCode);
        updateCountry.setName(updateCountryName);

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);
        personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

        personRestController.createPerson(traceId, personServiceCreatePersonRequestDto, authorization);

        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getValidPersonServiceUpdatePersonRequestDto();
        personServiceUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceUpdatePersonRequestDto.getCountry().setName(updateCountryName);
        personServiceUpdatePersonRequestDto.getCountry().setCode(updateCountryCode);
        personServiceUpdatePersonRequestDto.setFirstName(updateFirstName);
        personServiceUpdatePersonRequestDto.getAddress().setAddress(updateAddress);

        String json = OBJECT_MAPPER.writeValueAsString(personServiceUpdatePersonRequestDto);

//        Act & Assert
        mockMvc.perform(patch(PATH_UPDATE_PERSON)
                        .header("Content-Type", "application/json")
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void failUpdatePersonOnPersonNotFound() throws Exception {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String updateCountryCode = "AUS";
        String updateCountryName = "Australia";
        String updateFirstName = "TestFirstName";
        String updateAddress = "TestStreet";

        Country updateCountry = getValidCountry();
        updateCountry.setCode(updateCountryCode);
        updateCountry.setName(updateCountryName);

        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getValidPersonServiceUpdatePersonRequestDto();
        personServiceUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceUpdatePersonRequestDto.getCountry().setName(updateCountryName);
        personServiceUpdatePersonRequestDto.getCountry().setCode(updateCountryCode);
        personServiceUpdatePersonRequestDto.setFirstName(updateFirstName);
        personServiceUpdatePersonRequestDto.getAddress().setAddress(updateAddress);

        String json = OBJECT_MAPPER.writeValueAsString(personServiceUpdatePersonRequestDto);

//        Act & Assert
        mockMvc.perform(patch(PATH_UPDATE_PERSON)
                        .header("Content-Type", "application/json")
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void failUpdatePersonOnInvalidPersonServiceUpdatePersonRequestDto() throws Exception {
//        Arrange
        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getInvalidPersonServiceUpdatePersonRequestDto();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String json = OBJECT_MAPPER.writeValueAsString(personServiceUpdatePersonRequestDto);

//        Act & Assert
        mockMvc.perform(patch(PATH_UPDATE_PERSON)
                        .header("Content-Type", "application/json")
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void failUpdatePersonOnUnauthorizedRequestDto() throws Exception {
//        Arrange
        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getInvalidPersonServiceUpdatePersonRequestDto();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String json = OBJECT_MAPPER.writeValueAsString(personServiceUpdatePersonRequestDto);

//        Act & Assert
        mockMvc.perform(patch(PATH_UPDATE_PERSON)
                        .header("Content-Type", "application/json")
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}
