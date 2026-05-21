package aq.project.integration.transaction;

import aq.project.dto.ErrorDTO;
import aq.project.dto.OperationType;
import aq.project.dto.TransactionRequestDTO;
import aq.project.proxies.JwtClient;
import aq.project.util.RequestPropertyKeys;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Testcontainers
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableWireMock(@ConfigureWireMock(name = "transaction-service", port = 8083))
public class DoTransferTransactionIntegrationTest {

    @Value("${application.transaction-service.endpoints.send-transaction-request}")
    private String sendTransactionRequestUri;

    @Autowired
    private JwtClient jwtClient;
    @Autowired
    private WebTestClient webTestClient;

    @InjectWireMock("transaction-service")
    private WireMockServer transactionServiceMock;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties.registerApplicationContextContainerProperties(registry);
        registry.add("server.port", () -> "8585");
        registry.add("application.transaction-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successDoTransactionRequestDtoTest() {
//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.post(sendTransactionRequestUri)
                .willReturn(WireMock.ok()));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.post()
                .uri("/api/transactions/do-transaction")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .bodyValue(getValidWithdrawTransactionRequestDto())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void failOn5xxStatusTransactionServiceResponseTest() {
//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.post(sendTransactionRequestUri)
                .willReturn(WireMock.status(500)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.post()
                .uri("/api/transactions/do-transaction")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .bodyValue(getValidWithdrawTransactionRequestDto())
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOn4xxStatusTransactionServiceResponseTest() {
//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.post(sendTransactionRequestUri)
                .willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.post()
                .uri("/api/transactions/do-transaction")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .bodyValue(getValidWithdrawTransactionRequestDto())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    @Test
    public void failOnInvalidTransactionRequestDtoTest() {
//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.post(sendTransactionRequestUri)
                .willReturn(WireMock.status(400)));
//        Prepare test resources
        String adminAccessToken = jwtClient.requestAdminToken().block();
//        Test call
        webTestClient.post()
                .uri("/api/transactions/do-transaction")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .bodyValue(getInvalidWithdrawTransactionRequestDto())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class);
    }

    private TransactionRequestDTO getValidWithdrawTransactionRequestDto() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RequestPropertyKeys.SENDER_WALLET_ID, UUID.randomUUID().toString());
        properties.put(RequestPropertyKeys.RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        return new TransactionRequestDTO()
                .operationType(OperationType.WITHDRAW)
                .amount("85.58")
                .currency("RUB")
                .timestamp(System.currentTimeMillis())
                .properties(properties);
    }

    private TransactionRequestDTO getInvalidWithdrawTransactionRequestDto() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RequestPropertyKeys.SENDER_WALLET_ID, "invalid-id");
        properties.put(RequestPropertyKeys.RECIPIENT_WALLET_ID, "invalid-id");
        return new TransactionRequestDTO()
                .operationType(OperationType.WITHDRAW)
                .amount("-85.58")
                .currency("HELLO")
                .timestamp(-System.currentTimeMillis())
                .properties(properties);
    }
}
