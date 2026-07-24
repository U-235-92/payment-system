package aq.project.integration.transaction;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.ErrorDto;
import aq.project.services.TransactionService;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Assertions;
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
@EnableWireMock(@ConfigureWireMock(name = "transaction-service"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionStatusIntegrationTest {

    @Value("${application.transaction-service.endpoints.get-transaction-status}")
    private String getTransactionStatusUri;
    @Value("${application.individuals-api.endpoints.get-transaction-status}")
    private String individualsApiGetTransactionStatusEndpoint;

    @Autowired
    private KeycloakServiceClientFacade keycloakServiceClientFacade;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TransactionService transactionService;

    @InjectWireMock("transaction-service")
    private WireMockServer transactionServiceMock;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties.registerApplicationContextContainerProperties(registry);
        registry.add("application.transaction-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successGetTransactionStatusTest() {
//        Prepare mock service
        String transactionId = UUID.randomUUID().toString();
        transactionServiceMock.stubFor(WireMock.get(getTransactionStatusUri + "/" + transactionId)
                .willReturn(WireMock.ok()));

//        Test call
        Assertions.assertDoesNotThrow(() -> transactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failOn5xxStatusTransactionServiceResponseTest() {
//        Prepare mock service
        String transactionId = UUID.randomUUID().toString();
        transactionServiceMock.stubFor(WireMock.get(getTransactionStatusUri + "/" + transactionId)
                .willReturn(WireMock.status(500)));

//        Prepare test resources
        String adminJwtBearer = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

//        Test call
        webTestClient.get()
                .uri(individualsApiGetTransactionStatusEndpoint + "/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearer)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorDto.class);
    }

    @Test
    public void failOn4xxStatusTransactionServiceResponseTest() {
        String transactionId = UUID.randomUUID().toString();
//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.get(getTransactionStatusUri + "/" + transactionId)
                .willReturn(WireMock.status(400)));

//        Prepare test resources
        String adminJwtBearer = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

//        Test call
        webTestClient.get()
                .uri(individualsApiGetTransactionStatusEndpoint + "/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearer)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDto.class);
    }

    @Test
    public void failOnInvalidTransactionIdTest() {
//        Prepare mock service
        String transactionId = "invalid-id";
        transactionServiceMock.stubFor(WireMock.get(getTransactionStatusUri + "/" + transactionId)
                .willReturn(WireMock.ok()));

//        Prepare test resources
        String adminJwtBearer = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

//        Test call
        webTestClient.post()
                .uri(individualsApiGetTransactionStatusEndpoint + "/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearer)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDto.class);
    }

    @Test
    public void failOnNullTransactionIdTest() {
//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.get(getTransactionStatusUri + "/" + null)
                .willReturn(WireMock.status(400)));

//        Prepare test resources
        String adminJwtBearer = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

//        Test call
        webTestClient.post()
                .uri(individualsApiGetTransactionStatusEndpoint + "/" + null)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearer)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDto.class);
    }

    @Test
    public void failOnUnauthorizedCreateWalletRequestDtoTest() {
//        Prepare mock service
        String transactionId = UUID.randomUUID().toString();
        transactionServiceMock.stubFor(WireMock.get(getTransactionStatusUri + "/" + transactionId)
                .willReturn(WireMock.unauthorized()));

//        Test call
        webTestClient.get()
                .uri(individualsApiGetTransactionStatusEndpoint + "/" + transactionId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDto.class);
    }
}
