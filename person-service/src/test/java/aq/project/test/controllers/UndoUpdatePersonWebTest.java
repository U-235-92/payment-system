package aq.project.test.controllers;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.controllers.PersonRestController;
import aq.project.dto.PersonServiceCreatePersonRequestDto;
import aq.project.dto.PersonServiceUndoUpdatePersonRequestDto;
import aq.project.dto.PersonServiceUpdatePersonRequestDto;
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
import static aq.project.controller.PersonRestControllerApi.PATH_UNDO_UPDATE_PERSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UndoUpdatePersonWebTest {

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
    public void successUndoUpdatePerson() throws Exception {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String email = "test@mail.aq";

        String createCountryCode = "ATA";
        String createCountryName = "Antarctica";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);
        personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

        personRestController.createPerson(traceId, personServiceCreatePersonRequestDto, authorization);

        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getValidPersonServiceUpdatePersonRequestDto();
        personServiceUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceUpdatePersonRequestDto.setFirstName("UpdatedFirstName");

        personRestController.updatePerson(traceId, personServiceUpdatePersonRequestDto, authorization);

        PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto = getValidPersonServiceUndoUpdatePersonRequestDto();
        personServiceUndoUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

        String json = OBJECT_MAPPER.writeValueAsString(personServiceUndoUpdatePersonRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_UNDO_UPDATE_PERSON)
                        .header("Content-Type", "application/json")
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void failUndoUpdatePersonOnLastOperationIsNotUpdate() throws Exception {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String email = "test@mail.aq";

        String createCountryCode = "ATA";
        String createCountryName = "Antarctica";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);
        personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

        personRestController.createPerson(traceId, personServiceCreatePersonRequestDto, authorization);

        PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto = getValidPersonServiceUndoUpdatePersonRequestDto();
        personServiceUndoUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

        String json = OBJECT_MAPPER.writeValueAsString(personServiceUndoUpdatePersonRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_UNDO_UPDATE_PERSON)
                        .header("Content-Type", "application/json")
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void failUndoUpdatePersonOnInvalidRequestDto() throws Exception {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String email = "test@mail.aq";

        String createCountryCode = "ATA";
        String createCountryName = "Antarctica";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);
        personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

        personRestController.createPerson(traceId, personServiceCreatePersonRequestDto, authorization);

        PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto = getInvalidPersonServiceUndoUpdatePersonRequestDto();

        String json = OBJECT_MAPPER.writeValueAsString(personServiceUndoUpdatePersonRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_UNDO_UPDATE_PERSON)
                        .header("Content-Type", "application/json")
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void failUndoUpdatePersonOnUnauthorizedRequest() throws Exception {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String email = "test@mail.aq";

        String createCountryCode = "ATA";
        String createCountryName = "Antarctica";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);
        personServiceCreatePersonRequestDto.getCountry().setName(createCountryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(createCountryCode);

        personRestController.createPerson(traceId, personServiceCreatePersonRequestDto, authorization);

        PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto = getInvalidPersonServiceUndoUpdatePersonRequestDto();

        String json = OBJECT_MAPPER.writeValueAsString(personServiceUndoUpdatePersonRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_UNDO_UPDATE_PERSON)
                        .header("Content-Type", "application/json")
                        .header("x-trace-id", traceId)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}
