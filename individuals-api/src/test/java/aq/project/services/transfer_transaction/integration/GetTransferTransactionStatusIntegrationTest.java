package aq.project.services.transfer_transaction.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.transactions.TransferTransactionService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(name = "transaction-service", port = 18085))
public class GetTransferTransactionStatusIntegrationTest {

    @Value("${application.transaction-service.endpoints.get-transfer-transaction-status}")
    private String transactionServiceGetTransferTransactionStatusEndpoint;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("transaction-service")
    private WireMockServer transactionServiceMock;

    @Autowired
    private TransferTransactionService transferTransactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successGetTransferTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetTransferTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetTransferTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetTransferTransactionStatusEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(TransactionStatus.COMPLETED)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.getTransferTransactionStatus(transactionId).block());

        TransactionStatus recievedTransactionStatus = transferTransactionService.getTransferTransactionStatus(transactionId).block();

        Assertions.assertEquals(TransactionStatus.COMPLETED, recievedTransactionStatus);
    }

    @Test
    public void failGetTransferTransactionStatusOnTransactionService5xxError() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetTransferTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetTransferTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetTransferTransactionStatusEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> transferTransactionService.getTransferTransactionStatus(transactionId).block());
    }

    @Test
    public void failGetTransferTransactionStatusOnTransactionService4xxError() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetTransferTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetTransferTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetTransferTransactionStatusEndpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> transferTransactionService.getTransferTransactionStatus(transactionId).block());
    }

    @Test
    public void failGetTransferTransactionStatusOnNullTransactionId() {
//        Arrange
        UUID transactionId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transferTransactionService.getTransferTransactionStatus(transactionId).block());
    }

    @Test
    public void failGetTransferTransactionStatusOnFallback() {
//        Arrange
        final int RATE_LIMIT = 50;

        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetTransferTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetTransferTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetTransferTransactionStatusEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(TransactionStatus.COMPLETED)));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1) {
                new Thread(() -> transferTransactionService.getTransferTransactionStatus(transactionId).block()).start();
            } else {
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> transferTransactionService.getTransferTransactionStatus(transactionId).block());
            }
        }
    }
}
