package aq.project.integration.user;

import aq.project.dto.CreateUserDto;
import aq.project.services.UserService;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import static aq.project.util.TestDtoRepository.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "person-service"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateUserIntegrationTest {

    @Value("${application.person-service.endpoints.create-person}")
    private String personServiceCreatePersonEndpoint;

    @Value("${application.individuals-api.endpoints.create-user}")
    private String individualsApiCreatePersonEndpoint;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserService userService;

    @LocalServerPort
    private int port;

    @InjectWireMock("person-service")
    private WireMockServer personServiceMockServer;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties.registerApplicationContextContainerProperties(registry);
        registry.add("application.person-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @AfterEach
    public void cleanPersonServiceMock() {
        personServiceMockServer.resetAll();
    }

    @Test
    public void successCreateUserTest() {
        personServiceMockServer.stubFor(WireMock.post(personServiceCreatePersonEndpoint)
                .willReturn(WireMock.created()));

        CreateUserDto createUserDTO = getValidCreateUserDTO();

        Assertions.assertDoesNotThrow(() -> userService.createUser(createUserDTO));
    }

    @Test
    @Disabled("Test case connected with no valid admin client credentials. " +
            "To do this you have to change both [spring.security.oauth2.client.registration.keycloak.admin-id] " +
            "and [spring.security.oauth2.client.registration.keycloak.admin-secret] property values on any random value in " +
            "TestApplicationProperties.KeycloakProperties class in" +
            "registerApplicationContextContainerProperties() method")
    public void failCreateUserWithNoValidAdminClientCredentialsTest() {
        personServiceMockServer.stubFor(WireMock.post(personServiceCreatePersonEndpoint)
                .willReturn(WireMock.status(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        CreateUserDto createUserDTO = getValidCreateUserDTO();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserDTO)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @Disabled
    public void failCreateDuplicateUserTest() {
        personServiceMockServer.stubFor(WireMock.post(personServiceCreatePersonEndpoint)
                .willReturn(WireMock.status(HttpStatus.CONFLICT.value())));

        CreateUserDto createUserDTO = getDuplicateCreateUserDTO();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserDTO)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void failCreateUserWithNoMatchPasswordsTest() {
        CreateUserDto createUserDTO = getIncorrectCreateUserDTOWithDoNotMatchPasswords();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserDTO)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failCreateUserWithNullFieldsTest() {
        CreateUserDto createUserDTO = getIncorrectCreateUserDTOWithNullFields();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserDTO)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failCreateUserWithNullIndividualDataTest() {
        CreateUserDto createUserDTO = getIncorrectCreateUserDTOWithNullIndividualData();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserDTO)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}