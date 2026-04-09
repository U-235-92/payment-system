package aq.project.integration.end_to_end;

import aq.project.dto.CreateUserDTO;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static aq.project.util.TestDtoRepository.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateUserEndToEndIntegrationTest {

    @Value("${application.individuals-api.endpoints.create-user}")
    private String individualsApiCreatePersonEndpoint;

    @Autowired
    private WebTestClient webTestClient;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @Container
    private static final GenericContainer<?> PERSON_SERVICE_CONTAINER = TestContainers.PersonService.PERSON_SERVICE_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties
                .registerApplicationContextContainerProperties(registry);
        TestApplicationProperties.PersonServiceProperties
                .registerApplicationContextContainerProperties(registry);
    }

    @Test
    public void successCreateUserTest() {
        CreateUserDTO validCreateUserDTO = getValidCreateUserDTO();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validCreateUserDTO)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    @Disabled("Test case connected with no valid admin client credentials. " +
            "To do this you have to change both [spring.security.oauth2.client.registration.keycloak.admin-id] " +
            "and [spring.security.oauth2.client.registration.keycloak.admin-secret] property values on any random value in " +
            "TestApplicationProperties.KeycloakProperties class in" +
            "registerApplicationContextContainerProperties() method")
    public void failCreateUserWithNoValidAdminClientCredentialsTest() {
        CreateUserDTO validCreateUserDTO = getValidCreateUserDTO();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validCreateUserDTO)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    public void failCreateDuplicateUserTest() {
        CreateUserDTO Event = getDuplicateCreateUserDTO();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void failCreateUserWithNoMatchPasswordsTest() {
        CreateUserDTO Event = getIncorrectCreateUserDTOWithDoNotMatchPasswords();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failCreateUserWithNullFieldsTest() {
        CreateUserDTO Event = getIncorrectCreateUserDTOWithNullFields();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void failCreateUserWithNullIndividualDataTest() {
        CreateUserDTO Event = getIncorrectCreateUserDTOWithNullIndividualData();

        webTestClient.post()
                .uri(individualsApiCreatePersonEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Event)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
