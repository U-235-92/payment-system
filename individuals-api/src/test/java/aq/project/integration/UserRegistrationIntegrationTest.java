package aq.project.integration;

import aq.project.dto.UserRegistrationRequest;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
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
public class UserRegistrationIntegrationTest {

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
    public void testSuccessfulCreateUser() {
        UserRegistrationRequest request = getValidUserRegistrationRequest();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    private UserRegistrationRequest getValidUserRegistrationRequest() {
        return new UserRegistrationRequest()
                .email("valid@mail.aq")
                .username("username")
                .password("password")
                .confirmPassword("password")
                .firstName("firstName")
                .lastName("lastName");
    }

    @Test
    public void testDuplicateCreateUserFail() {
        UserRegistrationRequest request = getDuplicateUserRegistrationRequest();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private UserRegistrationRequest getDuplicateUserRegistrationRequest() {
        return new UserRegistrationRequest()
                .email("alice@post.aq")
                .username("alice")
                .password("password")
                .confirmPassword("password")
                .firstName("Alice")
                .lastName("K");
    }

    @Test
    public void testNoMatchPasswordsUserRegistrationRequest() {
        UserRegistrationRequest request = getIncorrectUserRegistrationRequestWithDoNotMatchPasswords();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private UserRegistrationRequest getIncorrectUserRegistrationRequestWithDoNotMatchPasswords() {
        return new UserRegistrationRequest()
                .email("bob@post.aq")
                .username("bob")
                .password("password")
                .confirmPassword("123")
                .firstName("Bob")
                .lastName("K");
    }

    @Test
    public void testNullFieldsUserRegistrationRequest() {
        UserRegistrationRequest request = getIncorrectUserRegistrationRequestWithNullFields();
        webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private UserRegistrationRequest getIncorrectUserRegistrationRequestWithNullFields() {
        return new UserRegistrationRequest()
                .email(null)
                .username(null)
                .password("password")
                .confirmPassword("123")
                .firstName("Bob")
                .lastName("K");
    }
}
