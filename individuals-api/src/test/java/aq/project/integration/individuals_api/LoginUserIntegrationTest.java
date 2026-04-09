package aq.project.integration.individuals_api;

import aq.project.dto.LoginUserDTO;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginUserIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties
                .registerApplicationContextContainerProperties(registry);
    }

    @Test
    public void successLoginUserTest() {
        webTestClient.post()
                .uri("/gateway/api/user/login-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginUserDTO().email("alice@post.aq").password("123"))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failLoginUserTest() {
        webTestClient.post()
                .uri("/gateway/api/user/login-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginUserDTO().email("novalid@post.aq").password("123"))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void failLoginUserWithNullRequestDataTest() {
        webTestClient.post()
                .uri("/gateway/api/user/login-user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginUserDTO().email(null).password("123"))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
