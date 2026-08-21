package aq.project.wallet_service_handler.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.utils.handlers.WalletServiceHandler;
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
    private static final String KEYCLOAK_SERVICE_GET_TOKEN_ENDPOINT = "/realms/payment-system/protocol/openid-connect/token";

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;
    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private WalletServiceHandler walletServiceHandler;

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

        String headerName = "Content-Type";
        String headerValue = "application/json";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(TransactionStatus.PENDING);

        ResponseDefinition responseDefinition = ResponseDefinitionBuilder.responseDefinition()
                .withStatus(200)
                .withHeader(headerName, headerValue)
                .withJsonBody(jsonNode)
                .build();

        walletServiceMockServer.stubFor(WireMock.get(endpoint)
                .willReturn(ResponseDefinitionBuilder.like(responseDefinition)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceHandler.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOn4xxErrorKeycloakServiceIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        walletServiceMockServer.stubFor(WireMock.get(KEYCLOAK_SERVICE_GET_TOKEN_ENDPOINT)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(ClientHttpException.class,
                () -> walletServiceHandler.getTransactionStatus(transactionId));

    }

    @Test
    public void failGetTransactionStatusOn5xxErrorKeycloakServiceAndSuccessFallbackIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        walletServiceMockServer.stubFor(WireMock.get(KEYCLOAK_SERVICE_GET_TOKEN_ENDPOINT)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> walletServiceHandler.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOn4xxErrorWalletServiceIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();
        String endpoint = String.format("%s/%s", WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT, transactionId);

        walletServiceMockServer.stubFor(WireMock.get(endpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(ClientHttpException.class,
                () -> walletServiceHandler.getTransactionStatus(transactionId));

    }

    @Test
    public void failGetTransactionStatusOn5xxErrorWalletServiceAndSuccessFallbackIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();
        String endpoint = String.format("%s/%s", WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT, transactionId);

        walletServiceMockServer.stubFor(WireMock.get(endpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> walletServiceHandler.getTransactionStatus(transactionId));

    }

    @Test
    public void failGetTransactionStatusOnNullTransactionIdIntegrationTest() {
//        Arrange
        String transactionId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletServiceHandler.getTransactionStatus(transactionId));

    }

    @Test
    public void failGetTransactionStatusOnInvalidTransactionIdIntegrationTest() {
//        Arrange
        String transactionId = "invalid-transaction-id";

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletServiceHandler.getTransactionStatus(transactionId));

    }
}
