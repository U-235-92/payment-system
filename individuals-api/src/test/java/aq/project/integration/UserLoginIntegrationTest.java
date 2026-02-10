package aq.project.integration;

import aq.project.dto.UserLoginRequest;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Disabled;
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
@Disabled
public class UserLoginIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties
                .registerApplicationContextContainerProperties(registry, KEYCLOAK);
    }

    @Test
    public void testSuccessUserLogin() {
        webTestClient.post()
                .uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginRequest().email("alice@post.aq").password("123"))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testFailUserLogin() {
        webTestClient.post()
                .uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginRequest().email("novalid@post.aq").password("123"))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testFailUserLoginWithNullRequestData() {
        webTestClient.post()
                .uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginRequest().email(null).password("123"))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
