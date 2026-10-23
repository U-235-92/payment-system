package aq.project.controllers.person_service;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceLoginPersonRequestDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import static aq.project._utils.entities.PersonServiceEntities.getInvalidIndividualsApiServiceLoginPersonRequestDto;
import static aq.project._utils.entities.PersonServiceEntities.getValidIndividualsApiServiceLoginPersonRequestDto;
import static aq.project.controller.PersonRestControllerApi.PATH_LOGIN_PERSON;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "person-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginPersonWebTest {

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
    public void successLoginPerson() {
//        Arrange
        IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto = getValidIndividualsApiServiceLoginPersonRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_LOGIN_PERSON)
                .header("Content-Type", "application/json")
                .bodyValue(loginPersonRequestDto)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failLoginPersonOnInvalidCredentials() {
//        Arrange
        IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto = getValidIndividualsApiServiceLoginPersonRequestDto();
        loginPersonRequestDto.setEmail("invalid@mail.aq");
        loginPersonRequestDto.setPassword("invalid");

//        Act & Assert
        webTestClient.post()
                .uri(PATH_LOGIN_PERSON)
                .header("Content-Type", "application/json")
                .bodyValue(loginPersonRequestDto)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failLoginPersonOnInvalidRequest() {
//        Arrange
        IndividualsApiServiceLoginPersonRequestDto loginPersonRequestDto = getInvalidIndividualsApiServiceLoginPersonRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_LOGIN_PERSON)
                .header("Content-Type", "application/json")
                .bodyValue(loginPersonRequestDto)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
