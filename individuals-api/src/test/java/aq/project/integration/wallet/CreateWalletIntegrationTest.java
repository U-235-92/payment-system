package aq.project.integration.wallet;

import aq.project.dto.CardType;
import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.ErrorDTO;
import aq.project.dto.WalletStatus;
import aq.project.clients.JwtClient;
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
public class CreateWalletIntegrationTest {

    @Value("${application.wallet-service.endpoints.create-wallet}")
    private String createWalletEndpointUri;

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
    public void successCreateWalletTest() {
//        Prepare mock service
        String createdWalletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri)
                .willReturn(WireMock.ok(createdWalletId)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.post()
                .uri("/api/v1/wallet/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .bodyValue(getValidCreateWalletRequestDTO())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failOn5xxStatusWalletServiceResponseTest() {
//        Prepare mock service
        String createdWalletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri)
                .willReturn(WireMock.status(500)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.post()
                .uri("/api/v1/wallet/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .bodyValue(getValidCreateWalletRequestDTO())
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOn4xxStatusWalletServiceResponseTest() {
//        Prepare mock service
        String createdWalletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri)
                .willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.post()
                .uri("/api/wallets/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .bodyValue(getValidCreateWalletRequestDTO())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOnInvalidCreateWalletRequestDtoTest() {
//        Prepare mock service
        String createdWalletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri)
                .willReturn(WireMock.status(500)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.post()
                .uri("/api/wallets/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .bodyValue(getInvalidCreateWalletRequestDTO())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOnUnauthorizedCreateWalletRequestDtoTest() {
//        Prepare mock service
        String createdWalletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri)
                .willReturn(WireMock.status(500)));
//        Test call
        webTestClient.post()
                .uri("/api/v1/wallet/create")
                .bodyValue(getValidCreateWalletRequestDTO())
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorDTO.class);
    }

    private CreateWalletRequestDTO getValidCreateWalletRequestDTO() {
        return new CreateWalletRequestDTO()
                .personId(UUID.randomUUID().toString())
                .walletStatus(WalletStatus.ACTIVE)
                .creator("Creator")
                .modifier("Modifier")
                .currencyCode("RUB")
                .balance("8585.85")
                .cardNumber("0000 0000 0000 0000")
                .cardCvvNumber("585")
                .cardExpirationDate("08/55")
                .cardType(CardType.VISA);
    }

    private CreateWalletRequestDTO getInvalidCreateWalletRequestDTO() {
        return new CreateWalletRequestDTO()
                .personId("person-id")
                .walletStatus(WalletStatus.ACTIVE)
                .creator("Creator")
                .modifier(null)
                .currencyCode("CODE")
                .balance("8585.8585")
                .cardNumber("0000 0000 hello 0000")
                .cardCvvNumber("585")
                .cardExpirationDate("08/55")
                .cardType(CardType.VISA);
    }
}
