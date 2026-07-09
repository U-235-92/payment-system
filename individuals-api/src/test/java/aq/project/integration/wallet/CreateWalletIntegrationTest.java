package aq.project.integration.wallet;

import aq.project.clients.KeycloakServiceWebClientFacade;
import aq.project.dto.CardType;
import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.ErrorDTO;
import aq.project.dto.WalletStatus;
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
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "wallet-service"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateWalletIntegrationTest {

    @Value("${application.wallet-service.endpoints.create-wallet}")
    private String createWalletEndpointUri;
    @Value("${application.individuals-api.endpoints.create-wallet}")
    private String individualsApiCreateWalletEndpoint;

    @Autowired
    private KeycloakServiceWebClientFacade keycloakServiceWebClientFacade;

    @Autowired
    private WebTestClient webTestClient;

    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties.registerApplicationContextContainerProperties(registry);
        registry.add("application.wallet-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successCreateWalletTest() {
        String createdWalletId = UUID.randomUUID().toString();
//        Prepare mock service
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri).willReturn(WireMock.ok(createdWalletId)));
//        Prepare test resources
        String adminJwtBearerHeader = keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();
//        Test call
        webTestClient.post()
                .uri(individualsApiCreateWalletEndpoint)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearerHeader)
                .bodyValue(getValidCreateWalletRequestDTO())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failOn5xxStatusWalletServiceResponseTest() {
//        Prepare mock service
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri).willReturn(WireMock.status(500)));
//        Prepare test resources
        String adminJwtBearerHeader = keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();
//        Test call
        webTestClient.post()
                .uri(individualsApiCreateWalletEndpoint)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearerHeader)
                .bodyValue(getValidCreateWalletRequestDTO())
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOn4xxStatusWalletServiceResponseTest() {
//        Prepare mock service
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri).willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminJwtBearerHeader = keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();
//        Test call
        webTestClient.post()
                .uri(individualsApiCreateWalletEndpoint)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearerHeader)
                .bodyValue(getValidCreateWalletRequestDTO())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOnInvalidCreateWalletRequestDtoTest() {
//        Prepare mock service
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri).willReturn(WireMock.status(500)));
//        Prepare test resources
        String adminJwtBearerHeader = keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();
//        Test call
        webTestClient.post()
                .uri(individualsApiCreateWalletEndpoint)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearerHeader)
                .bodyValue(getInvalidCreateWalletRequestDTO())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOnUnauthorizedCreateWalletRequestDtoTest() {
//        Prepare mock service
        walletServiceMock.stubFor(WireMock.post(createWalletEndpointUri).willReturn(WireMock.status(500)));
//        Test call
        webTestClient.post()
                .uri(individualsApiCreateWalletEndpoint)
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
