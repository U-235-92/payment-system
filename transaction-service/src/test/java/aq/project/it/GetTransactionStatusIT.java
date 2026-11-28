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
public class GetTransactionStatusIT {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

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

        UUID transactionId = transactionRepository.save(newTransaction).getId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failureOnTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.TRANSFER);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> transactionService.getTransactionStatus(transactionId));
    }
}