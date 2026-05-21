package aq.project.integration.wallet;

import aq.project.dto.ErrorDTO;
import aq.project.proxies.JwtClient;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.UUID;

@Testcontainers
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "wallet-service", port = 8083))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class GetWalletInfoIntegrationTest {

    @Value("${application.wallet-service.endpoints.get-wallet-info}")
    private String getWalletInfoEndpointUri;

    @Autowired
    private JwtClient jwtClient;
    @Autowired
    private WebTestClient webTestClient;

    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties.registerApplicationContextContainerProperties(registry);
        registry.add("server.port", () -> "8585");
        registry.add("application.wallet-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successGetWalletInfoTest() {
//        Prepare mock service
        String walletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + walletId)
                .willReturn(WireMock.ok()));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.get()
                .uri("/api/wallets/info/" + walletId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failOn5xxStatusWalletServiceResponseTest() {
//        Prepare mock service
        String walletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + walletId)
                .willReturn(WireMock.status(500)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.get()
                .uri("/api/wallets/info/" + walletId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOn4xxStatusWalletServiceResponseTest() {
//        Prepare mock service
        String walletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + walletId)
                .willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.get()
                .uri("/api/wallets/info/" + walletId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOnNullWalletIdTest() {
//        Prepare mock service
        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + null)
                .willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.get()
                .uri("/api/wallets/info/" + null)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOnInvalidWalletIdTest() {
//        Prepare mock service
        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + "invalid-id")
                .willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.get()
                .uri("/api/wallets/info/" + "invalid-id")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }
}
