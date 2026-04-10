package aq.project.integration.mock;

import aq.project.dto.LoginUserDTO;
import aq.project.dto.UserInfoResponseDTO;
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
import org.springframework.http.ResponseEntity;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static aq.project.util.TestDtoRepository.getLoginUserDTO;
import static aq.project.util.TestUtils.getWebClient;
import static aq.project.util.TestUtils.loginUserMono;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "person-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetUserInfoWithMockPersonServiceIntegrationTest {

    @Value("${application.individuals-api.test.actual-user-keycloak-id}")
    private String actualUserKeycloakId;

    @Value("${application.person-service.endpoints.get-person-info-by-keycloak-id}")
    private String personServiceGetPersonInfoEndpoint;

    @Value("${application.individuals-api.endpoints.get-user-info}")
    private String individualsApiGetUserInfoEndpoint;

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @InjectWireMock("person-service-mock")
    private WireMockServer personServiceMockServer;

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
    public void successGetUserInfoTest() {
        personServiceMockServer.stubFor(WireMock.get(personServiceGetPersonInfoEndpoint + actualUserKeycloakId)
                .willReturn(WireMock.ok()));

        WebClient webClient = getWebClient(port);

        LoginUserDTO loginUserDTO = getLoginUserDTO("alice@post.aq", "123");

        Mono<ResponseEntity<Void>> responseEntityMono = loginUserMono(loginUserDTO, webClient)
                .flatMap(responseEntityTokenDto -> webClient.get()
                        .uri(individualsApiGetUserInfoEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + responseEntityTokenDto.getBody().getAccessToken())
                        .exchangeToMono(responseEntityUserInfoDto -> responseEntityUserInfoDto.bodyToMono(UserInfoResponseDTO.class))
                        .map(response -> ResponseEntity.ok().build()));

        StepVerifier.create(responseEntityMono)
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    public void failGetUserInfoWithNoAccessTokenTest() {
        webTestClient.get()
                .uri(individualsApiGetUserInfoEndpoint)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void failGetUserInfoWithInvalidAccessTokenTest() {
        webTestClient.get()
                .uri(individualsApiGetUserInfoEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "wrong-access-token")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
