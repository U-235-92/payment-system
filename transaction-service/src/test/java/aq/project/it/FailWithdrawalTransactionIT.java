package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.PaymentType;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
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

import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FailWithdrawalTransactionIT {

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
    public void successFailTransaction() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.WITHDRAWAL);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.failTransaction(transactionId));

        Transaction savedTransaction = transactionRepository.findById(transactionId).get();

        Assertions.assertEquals(TransactionStatus.FAILED, savedTransaction.getTransactionStatus());
    }

    @Test
    public void successOnTransactionIsNotPending() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
        newTransaction.setPaymentType(PaymentType.WITHDRAWAL);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.failTransaction(transactionId));
    }

    @Test
    public void successOnTransactionIsTransfer() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
        newTransaction.setPaymentType(PaymentType.TRANSFER);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.failTransaction(transactionId));
    }

    @Test
    public void failureOnTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transactionService.failTransaction(transactionId));
    }

    @Test
    public void failureOnRateLimiterExceeded() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.WITHDRAWAL);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> transactionService.failTransaction(transactionId));
    }
}