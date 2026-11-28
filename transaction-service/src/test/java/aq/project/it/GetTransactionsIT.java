package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.PaymentType;
import aq.project.dto.TransactionInfoResponseDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TransactionService;
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

import java.util.List;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionsIT {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

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
    }

    @Test
    public void successGetTransactionStatus() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.TRANSFER);

        transactionRepository.save(newTransaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.getTransactions(
                null, null, null, null, null, null, null, null));

        List<TransactionInfoResponseDto> transactionInfoList = transactionService.getTransactions(
                null, null, null, null, null, null, null, null);

        Assertions.assertFalse(transactionInfoList.isEmpty());
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.TRANSFER);

        transactionRepository.save(newTransaction);

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> transactionService.getTransactions(
                        null, null, null, null, null, null, null, null));
    }
}