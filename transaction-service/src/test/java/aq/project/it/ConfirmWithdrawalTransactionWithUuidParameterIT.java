package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.ErrorDto;
import aq.project.dto.PaymentType;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TransactionService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Assertions;
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
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(value = @ConfigureWireMock(name = "wallet-service", port = 18085))
public class ConfirmWithdrawalTransactionWithUuidParameterIT {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);

        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);

        registry.add("app.service.wallet-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successOnTransactionIsPendingAndWalletService2xxResponse() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.WITHDRAWAL);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

        String withdrawalWalletEndpoint = "/api/v1/wallet/withdrawal";

        walletServiceMock.stubFor(WireMock.post(withdrawalWalletEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(newTransaction.getWalletId())));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.confirmTransaction(transactionId));

        Transaction savedTransaction = transactionRepository.findById(transactionId).get();

        Assertions.assertEquals(TransactionStatus.COMPLETED, savedTransaction.getTransactionStatus());
    }

    @Test
    public void successOnTransactionIsNotPending() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
        newTransaction.setPaymentType(PaymentType.WITHDRAWAL);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.confirmTransaction(transactionId));
    }

    @Test
    public void successOnTransactionIsTransfer() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
        newTransaction.setPaymentType(PaymentType.TRANSFER);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.confirmTransaction(transactionId));
    }

    @Test
    public void failureOnTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transactionService.confirmTransaction(transactionId));
    }

    @Test
    public void failureOnTransactionIsPendingAndWalletService4xxResponse() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.WITHDRAWAL);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

        String withdrawalWalletEndpoint = "/api/v1/wallet/withdrawal";

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        walletServiceMock.stubFor(WireMock.post(withdrawalWalletEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> transactionService.confirmTransaction(transactionId));
    }

    @Test
    public void failureOnTransactionIsPendingAndWalletService5xxResponse() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.WITHDRAWAL);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

        String withdrawalWalletEndpoint = "/api/v1/wallet/withdrawal";

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        walletServiceMock.stubFor(WireMock.post(withdrawalWalletEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> transactionService.confirmTransaction(transactionId));
    }

    @Test
    public void failureOnRetryExceeded() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.WITHDRAWAL);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

        String withdrawalWalletEndpoint = "/api/v1/wallet/withdrawal";

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        walletServiceMock.stubFor(WireMock.post(withdrawalWalletEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        FallbackAssert.assertThrowsRetryLimit(FallbackOperationException.class,
                () -> transactionService.confirmTransaction(transactionId));
    }
}
