package aq.project.test.controllers;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.controllers.PersonRestController;
import aq.project.dto.PersonServiceCreatePersonRequestDto;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.repositories.ServiceOperationRepository;
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

import static aq.project._utils.entities.PersonServiceEntities.getValidPersonServiceCreatePersonRequestDto;
import static aq.project.controller.PersonRestControllerApi.PATH_GET_PERSON_INFO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetPersonInfoWebTest {

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
    public void successGetPersonInfoPerson() throws Exception {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

        String email = "test@mail.aq";

        String countryCode = "ATA";
        String countryName = "Antarctica";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setPersonKeycloakId(personKeycloakId);
        personServiceCreatePersonRequestDto.setEmail(email);
        personServiceCreatePersonRequestDto.getCountry().setName(countryName);
        personServiceCreatePersonRequestDto.getCountry().setCode(countryCode);

        personRestController.createPerson(traceId, personServiceCreatePersonRequestDto, authorization);

//        Act & Assert
        mockMvc.perform(get(PATH_GET_PERSON_INFO.replace("{personKeycloakId}", personKeycloakId.toString()))
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void failGetPersonInfoOnPersonNotFound() throws Exception {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(get(PATH_GET_PERSON_INFO.replace("{personKeycloakId}", personKeycloakId.toString()))
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId))
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void failGetPersonInfoOnInvalidRequest() throws Exception {
//        Arrange
        String personKeycloakId = "wrong-id";

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(get(PATH_GET_PERSON_INFO.replace("{personKeycloakId}", personKeycloakId))
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId))
                .andExpect(status().isInternalServerError());

    }

    @Test
    public void failGetPersonInfoOnUnauthorizedRequest() throws Exception {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        String traceId = UUID.randomUUID().toString();
        String authorization = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(get(PATH_GET_PERSON_INFO.replace("{personKeycloakId}", personKeycloakId.toString()))
                        .header("Authorization", authorization)
                        .header("x-trace-id", traceId))
                .andExpect(status().isUnauthorized());

    }
}
