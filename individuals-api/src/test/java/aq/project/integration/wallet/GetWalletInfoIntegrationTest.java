package aq.project.integration.wallet;

import aq.project.clients.KeycloakServiceWebClientFacade;
import aq.project.dto.CardType;
import aq.project.dto.ErrorDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static aq.project.util.constants.CustomConstants.ISO_DATE_FORMAT;

@Testcontainers
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "wallet-service"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWalletInfoIntegrationTest {

    @Value("${application.wallet-service.endpoints.get-wallet-info}")
    private String getWalletInfoEndpointUri;
    @Value("${application.individuals-api.endpoints.get-wallet-info}")
    private String individualsApiGetWalletInfoEndpoint;

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
        registry.add("server.port", () -> "8585");
        registry.add("application.wallet-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successGetWalletInfoTest() {
//        Prepare mock service
        String walletId = UUID.randomUUID().toString();

        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + "/" + walletId)
                .willReturn(ResponseDefinitionBuilder.okForJson(getValidWalletInfoResponseDto(walletId))));
//        Prepare test resources
        String adminJwtBearerHeader = keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();
//        Test call
        webTestClient.get()
                .uri(individualsApiGetWalletInfoEndpoint + "/" + walletId)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearerHeader)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failOn5xxStatusWalletServiceResponseTest() {
//        Prepare mock service
        String walletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + "/" + walletId)
                .willReturn(WireMock.status(500)));
//        Prepare test resources
        String adminJwtBearerHeader = keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();
//        Test call
        webTestClient.get()
                .uri(individualsApiGetWalletInfoEndpoint + "/" + walletId)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearerHeader)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOn4xxStatusWalletServiceResponseTest() {
//        Prepare mock service
        String walletId = UUID.randomUUID().toString();
        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + "/" + walletId)
                .willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminJwtBearerHeader = keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();
//        Test call
        webTestClient.get()
                .uri(individualsApiGetWalletInfoEndpoint + "/" + walletId)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearerHeader)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOnNullWalletIdTest() {
//        Prepare mock service
        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + "/" + null)
                .willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminJwtBearerHeader = keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();
//        Test call
        webTestClient.get()
                .uri( individualsApiGetWalletInfoEndpoint + "/" + null)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearerHeader)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOnInvalidWalletIdTest() {
//        Prepare mock service
        walletServiceMock.stubFor(WireMock.get(getWalletInfoEndpointUri + "/" + "invalid-id")
                .willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminJwtBearerHeader = keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();
//        Test call
        webTestClient.get()
                .uri( individualsApiGetWalletInfoEndpoint + "/" + "invalid-id")
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearerHeader)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    private WalletInfoResponseDTO getValidWalletInfoResponseDto(String walletId) {
        String date = Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(ISO_DATE_FORMAT));

        WalletInfoResponseDTO walletInfoResponseDTO = new WalletInfoResponseDTO();
        walletInfoResponseDTO.setWalletId(walletId);
        walletInfoResponseDTO.setBalance("85.58");
        walletInfoResponseDTO.setCardNumber("1234 5678 9012 3456");
        walletInfoResponseDTO.setCardType(CardType.VISA);
        walletInfoResponseDTO.setCardExpirationDate("08/85");
        walletInfoResponseDTO.setCurrencyCode("RUB");
        walletInfoResponseDTO.setCreatedAt(date);
        walletInfoResponseDTO.setModifiedAt(date);
        walletInfoResponseDTO.setPersonId(UUID.randomUUID().toString());
        return walletInfoResponseDTO;
    }
}
