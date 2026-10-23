package aq.project.controllers.person_service;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceCreatePersonRequestDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static aq.project._utils.entities.PersonServiceEntities.getInvalidIndividualsApiServiceCreatePersonRequestDto;
import static aq.project._utils.entities.PersonServiceEntities.getValidIndividualsApiServiceCreatePersonRequestDto;
import static aq.project.controller.PersonRestControllerApi.PATH_CREATE_PERSON;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "person-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePersonWebTest {

    @Value("${application.person-service.endpoints.create-person}")
    private String createPersonUri;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("person-service")
    private WireMockServer personServiceMock;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.person-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successCreatePerson() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getValidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setEmail("person@mail.aq");
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.ok()));

//        Act & Assert
        webTestClient.post()
                .uri(createPersonUri)
                .header("Content-Type", "application/json")
                .bodyValue(createPersonRequestDto)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failCreatePersonOnDuplicateCreatePersonRequest() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getValidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setEmail("person@mail.aq");
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.ok()));

//        Act & Assert
        webTestClient.post()
                .uri(createPersonUri)
                .header("Content-Type", "application/json")
                .bodyValue(createPersonRequestDto)
                .exchange();

        webTestClient.post()
                .uri(createPersonUri)
                .header("Content-Type", "application/json")
                .bodyValue(createPersonRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failCreatePersonOnPersonService4xxPersonServiceError() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getValidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setEmail("person@mail.aq");
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.badRequest()));

//        Act & Assert
        webTestClient.post()
                .uri(createPersonUri)
                .header("Content-Type", "application/json")
                .bodyValue(createPersonRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failCreatePersonOnPersonService5xxPersonServiceError() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getValidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setEmail("person@mail.aq");
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

        personServiceMock.stubFor(WireMock.post(PATH_CREATE_PERSON).willReturn(WireMock.serverError()));

//        Act & Assert
        webTestClient.post()
                .uri(createPersonUri)
                .header("Content-Type", "application/json")
                .bodyValue(createPersonRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    public void failCreatePersonOnInvalidRequest() {
//        Arrange
        IndividualsApiServiceCreatePersonRequestDto createPersonRequestDto = getInvalidIndividualsApiServiceCreatePersonRequestDto();
        createPersonRequestDto.setEmail("person@mail.aq");
        createPersonRequestDto.setFirstName("Firstname");
        createPersonRequestDto.setLastName("Lastname");

//        Act & Assert
        webTestClient.post()
                .uri(createPersonUri)
                .header("Content-Type", "application/json")
                .bodyValue(createPersonRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }
}
