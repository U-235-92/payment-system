package aq.project.transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.services.TransactionService;
import aq.project.utils.telemetry.TraceContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.security.SecureRandom;
import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "wallet-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionStatusIntegrationTest {

    private static final String WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT = "/api/v1/transaction/status";

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;
    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TraceContext traceContext;

    @InjectWireMock("wallet-service-mock")
    private WireMockServer walletServiceMockServer;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        registry.add("service.wallet-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @BeforeEach
    public void propagateTraceId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder xTraceId = new StringBuilder();
        for(byte b : bytes) {
            xTraceId.append(String.format("%02x", b));
        }
        traceContext.setTraceId(xTraceId.toString());
    }

    @AfterEach
    public void cleanTraceContext() {
        traceContext.clean();
    }

    @Test
    public void successGetTransactionStatusIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();
        String endpoint = String.format("%s/%s", WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT, transactionId);

        String contentTypeHeaderName = "Content-Type";
        String contentTypeHeaderValue = "application/json";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(TransactionStatus.PENDING);

        ResponseDefinition responseDefinition = ResponseDefinitionBuilder.responseDefinition()
                .withStatus(200)
                .withHeader(contentTypeHeaderName, contentTypeHeaderValue)
                .withJsonBody(jsonNode)
                .build();

        walletServiceMockServer.stubFor(WireMock.get(endpoint)
                .willReturn(ResponseDefinitionBuilder.like(responseDefinition)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOnNullTransactionIdIntegrationTest() {
//        Arrange
        String transactionId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOnInvalidTransactionIdIntegrationTest() {
//        Arrange
        String transactionId = "invalid-transaction-id";

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.getTransactionStatus(transactionId));
    }
}
