package aq.project.integration.mock;

import aq.project.dto.LoginUserDTO;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static aq.project.util.TestDtoRepository.*;
import static aq.project.util.TestUtils.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "person-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeleteUserWithMockPersonServiceIntegrationTest {

    @Value("${application.individuals-api.test.actual-user-keycloak-id}")
    private String actualUserKeycloakId;

    @Value("${application.individuals-api.test.unknown-user-keycloak-id}")
    private String unknownUserKeycloakId;

    @Value("${application.person-service.endpoints.delete-person-by-keycloak-id}")
    private String personServiceDeletePersonEndpoint;

    @Value("${application.individuals-api.endpoints.delete-user-by-keycloak-id}")
    private String individualsApiDeletePersonEndpoint;

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @InjectWireMock("person-service-mock")
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
    public void successDeleteUserTest() {
        personServiceMockServer.stubFor(WireMock.delete(personServiceDeletePersonEndpoint + actualUserKeycloakId)
                .willReturn(WireMock.ok()));

        LoginUserDTO loginUserDTO = getLoginUserDTO("alice@post.aq", "123");

        WebClient webClient = getWebClient(port);

        String accessToken = loginUserMono(loginUserDTO, webClient).block().getBody().getAccessToken();

        webTestClient.delete()
                .uri(individualsApiDeletePersonEndpoint + actualUserKeycloakId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failDeleteUserWithUnknownUserKeycloakIdTest() {
        personServiceMockServer.stubFor(WireMock.delete(personServiceDeletePersonEndpoint + unknownUserKeycloakId)
                .willReturn(WireMock.badRequest()));

        LoginUserDTO loginUserDTO = getLoginUserDTO("alexander@post.aq", "123");

        WebClient webClient = getWebClient(port);

        String accessToken = loginUserMono(loginUserDTO, webClient).block().getBody().getAccessToken();

        webTestClient.delete()
                .uri(individualsApiDeletePersonEndpoint + unknownUserKeycloakId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failDeleteUserWithEmptyKeycloakIdTest() {
        LoginUserDTO loginUserDTO = getLoginUserDTO("alexander@post.aq", "123");

        WebClient webClient = getWebClient(port);

        String accessToken = loginUserMono(loginUserDTO, webClient).block().getBody().getAccessToken();

        webTestClient.delete()
                .uri(individualsApiDeletePersonEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void failDeleteUserWithNoValidKeycloakIdTest() {
        LoginUserDTO loginUserDTO = getLoginUserDTO("alexander@post.aq", "123");

        WebClient webClient = getWebClient(port);

        String accessToken = loginUserMono(loginUserDTO, webClient).block().getBody().getAccessToken();

        webTestClient.delete()
                .uri(individualsApiDeletePersonEndpoint + "no_valid_keycloak_id")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
