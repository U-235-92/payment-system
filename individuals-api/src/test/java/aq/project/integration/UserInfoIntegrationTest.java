package aq.project.integration;

import aq.project.controllers.AuthRestControllerV1;
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
public class UserInfoIntegrationTest {

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
    public void testSuccessGetUserInfo() throws IncorrectUserCredentialsException {
        UserLoginRequest userLoginRequest = new UserLoginRequest()
                .email("alexander@post.aq")
                .password("123");
        TokenResponse tokenResponse = authController.requestToken(userLoginRequest)
                .block()
                .getBody();
        webTestClient.get()
                .uri("/v1/auth/me")
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testFailGetUserInfoWithNoAccessToken() {
        webTestClient.get()
                .uri("/v1/auth/me")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testFailGetUserInfoWithInvalidAccessToken() {
        String wrongAccessToken = "wrong-access-token";
        webTestClient.get()
                .uri("/v1/auth/me")
                .header("Authorization", "Bearer " + wrongAccessToken)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
