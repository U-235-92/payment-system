package aq.project.services.withdraw_transaction.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.transactions.WithdrawTransactionService;
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
public class GetWithdrawTransactionStatusIntegrationTest {

    @Value("${application.transaction-service.endpoints.get-withdraw-transaction-status}")
    private String transactionServiceGetWithdrawTransactionStatusEndpoint;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("transaction-service")
    private WireMockServer transactionServiceMock;

    @Autowired
    private WithdrawTransactionService withdrawTransactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successGetWithdrawTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetWithdrawTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetWithdrawTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetWithdrawTransactionStatusEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(TransactionStatus.COMPLETED)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block());

        TransactionStatus recievedTransactionStatus = withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block();

        Assertions.assertEquals(TransactionStatus.COMPLETED, recievedTransactionStatus);
    }

    @Test
    public void failGetWithdrawTransactionStatusOnTransactionService5xxError() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetWithdrawTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetWithdrawTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetWithdrawTransactionStatusEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block());
    }

    @Test
    public void failGetWithdrawTransactionStatusOnTransactionService4xxError() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetWithdrawTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetWithdrawTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetWithdrawTransactionStatusEndpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block());
    }

    @Test
    public void failGetWithdrawTransactionStatusOnNullTransactionId() {
//        Arrange
        UUID transactionId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block());
    }

    @Test
    public void failGetWithdrawTransactionStatusOnFallback() {
//        Arrange
        final int RATE_LIMIT = 50;

        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetWithdrawTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetWithdrawTransactionStatusEndpoint, transactionId);

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetWithdrawTransactionStatusEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(TransactionStatus.COMPLETED)));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1) {
                new Thread(() -> withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block()).start();
            } else {
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block());
            }
        }
    }
}
