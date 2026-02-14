package aq.project.integration;

import aq.project.controllers.AuthRestControllerV1;
import aq.project.dto.TokenRefreshRequest;
import aq.project.dto.TokenResponse;
import aq.project.dto.UserLoginRequest;
import aq.project.exceptions.IncorrectUserCredentialsException;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateTokenIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AuthRestControllerV1 authController;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties
                .registerApplicationContextContainerProperties(registry, KEYCLOAK);
    }

    @Test
    public void testSuccessUpdateToken() throws IncorrectUserCredentialsException {
        UserLoginRequest loginRequest = new UserLoginRequest().email("alice@post.aq").password("123");
        TokenResponse tokenResponse = authController.requestToken(loginRequest).block().getBody();
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest().refreshToken(tokenResponse.getRefreshToken());
        webTestClient.post()
                .uri("/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tokenRefreshRequest)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testFailNullUpdateToken() throws IncorrectUserCredentialsException {
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest().refreshToken(null);
        webTestClient.post()
                .uri("/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tokenRefreshRequest)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void testFailWrongUpdateToken() throws IncorrectUserCredentialsException {
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest().refreshToken("wrong-token");
        webTestClient.post()
                .uri("/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tokenRefreshRequest)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
