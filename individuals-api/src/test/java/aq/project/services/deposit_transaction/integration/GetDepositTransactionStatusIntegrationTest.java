package aq.project.services.deposit_transaction.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.transactions.DepositTransactionService;
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
public class GetDepositTransactionStatusIntegrationTest {

    @Value("${application.transaction-service.endpoints.get-deposit-transaction-status}")
    private String transactionServiceGetDepositTransactionStatusEndpoint;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("transaction-service")
    private WireMockServer transactionServiceMock;

    @Autowired
    private DepositTransactionService depositTransactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successGetDepositTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetDepositTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetDepositTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetDepositTransactionStatusEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(TransactionStatus.COMPLETED)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.getDepositTransactionStatus(transactionId).block());

        TransactionStatus recievedTransactionStatus = depositTransactionService.getDepositTransactionStatus(transactionId).block();

        Assertions.assertEquals(TransactionStatus.COMPLETED, recievedTransactionStatus);
    }

    @Test
    public void failGetDepositTransactionStatusOnTransactionService5xxError() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetDepositTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetDepositTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetDepositTransactionStatusEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> depositTransactionService.getDepositTransactionStatus(transactionId).block());
    }

    @Test
    public void failGetDepositTransactionStatusOnTransactionService4xxError() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetDepositTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetDepositTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetDepositTransactionStatusEndpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> depositTransactionService.getDepositTransactionStatus(transactionId).block());
    }

    @Test
    public void failGetDepositTransactionStatusOnNullTransactionId() {
//        Arrange
        UUID transactionId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.getDepositTransactionStatus(transactionId).block());
    }

    @Test
    public void failGetDepositTransactionStatusOnFallback() {
//        Arrange
        final int RATE_LIMIT = 50;

        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetDepositTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetDepositTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetDepositTransactionStatusEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(TransactionStatus.COMPLETED)));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1) {
                new Thread(() -> depositTransactionService.getDepositTransactionStatus(transactionId).block()).start();
            } else {
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> depositTransactionService.getDepositTransactionStatus(transactionId).block());
            }
        }
    }
}
