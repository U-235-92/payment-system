package aq.project.integration.user;

import aq.project.dto.LoginUserDto;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${application.individuals-api.endpoints.login-user}")
    private String individualsApiLoginUserEndpoint;

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
                .uri(individualsApiLoginUserEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginUserDto().email("alice@post.aq").password("123"))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failLoginUserTest() {
        webTestClient.post()
                .uri(individualsApiLoginUserEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginUserDto().email("novalid@post.aq").password("123"))
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failLoginUserWithNullRequestDataTest() {
        webTestClient.post()
                .uri(individualsApiLoginUserEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginUserDto().email(null).password("123"))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
