package aq.project.integration;

import aq.project.dto.LoginUserDTO;
import aq.project.dto.ResponseTokenDTO;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeleteUserIntegrationTest {

    @Value("${application.individuals-api.test.actual-user-keycloak-id}")
    private String actualUserKeycloakId;

    @Value("${application.individuals-api.test.unknown-user-keycloak-id}")
    private String unknownUserKeycloakId;

    @LocalServerPort
    private int port;

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
    public void successDeleteUserTest() {
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("alice@post.aq");
        loginUserDTO.setPassword("123");

        WebClient webClient = getWebClient();

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO)
                .flatMap(responseEntity -> webClient.delete()
                        .uri("/gateway/api/user/delete-user-by-keycloak-id/" + actualUserKeycloakId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntity.getBody().getAccessToken())
                        .exchangeToMono(response -> {
                            if(response.statusCode().is2xxSuccessful())
                                return Mono.just(ResponseEntity.ok().build());
                            return Mono.just(ResponseEntity.status(response.statusCode()).build());
                        }));
        StepVerifier.create(updateUserMono)
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    public void failDeleteUserWithUnknownUserKeycloakIdTest() {
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("alice@post.aq");
        loginUserDTO.setPassword("123");

        WebClient webClient = getWebClient();

        Mono<ResponseEntity<Void>> updateUserMono = loginUserMono(loginUserDTO)
                .flatMap(responseEntity -> webClient.delete()
                        .uri("/gateway/api/user/delete-user-by-keycloak-id/" + unknownUserKeycloakId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntity.getBody().getAccessToken())
                        .exchangeToMono(response -> {
                            if(response.statusCode().is2xxSuccessful())
                                return Mono.just(ResponseEntity.ok().build());
                            return Mono.just(ResponseEntity.status(response.statusCode()).build());
                        }));
        StepVerifier.create(updateUserMono)
                .expectNextMatches(response -> response.getStatusCode().is4xxClientError());
    }

    @Test
    public void failUpdateUserWithEmptyKeycloakIdTest() {
        webTestClient.delete()
                .uri("/gateway/api/user/delete-user-by-keycloak-id/")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void failUpdateUserWithNoValidKeycloakIdTest() {
                webTestClient.delete()
                .uri("/gateway/api/user/delete-user-by-keycloak-id/no_valid_keycloak_id")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private Mono<ResponseEntity<ResponseTokenDTO>> loginUserMono(LoginUserDTO loginUserDTO) {
        WebClient webClient = getWebClient();
        return webClient.post()
                .uri("/gateway/api/user/login-user")
                .bodyValue(loginUserDTO)
                .exchangeToMono(response -> response.bodyToMono(ResponseTokenDTO.class))
                .map(response -> ResponseEntity.ok().body(response));
    }

    private WebClient getWebClient() {
        return WebClient.builder().baseUrl("http://localhost:" + port).build();
    }
}
