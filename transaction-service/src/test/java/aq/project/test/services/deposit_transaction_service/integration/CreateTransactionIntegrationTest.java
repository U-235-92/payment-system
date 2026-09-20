package aq.project.test.services.deposit_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.services.DepositTransactionService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
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

import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getInvalidTransactionServiceDepositTransaction;
import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateTransactionIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    @Autowired
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Autowired
    private DepositTransactionService transactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void cleanRepositories() {
        transactionServiceDepositTransactionRepository.deleteAll();
        paymentProviderServiceTransactionRequestRepository.deleteAll();
    }

    @Test
    public void successCreateTransaction() {
//        Arrange
        TransactionServiceDepositTransaction transaction = getValidTransactionServiceDepositTransaction();
        transaction.setId(null);
        transaction.getTransactionMetadata().setId(null);
        transaction.getTransactionMetadata().setTimestamp(null);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.createTransaction(transaction));
        Assertions.assertNotNull(transactionService.createTransaction(transaction));
    }

    @Test
    public void failCreateTransactionOnInvalidTransactionServiceDepositTransaction() {
//        Arrange
        TransactionServiceDepositTransaction transaction = getInvalidTransactionServiceDepositTransaction();
        transaction.setId(null);
        transaction.getTransactionMetadata().setId(null);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.createTransaction(transaction));
    }

    @Test
    public void failCreateTransactionOnNullTransactionServiceDepositTransaction() {
//        Arrange
        TransactionServiceDepositTransaction transaction = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.createTransaction(transaction));
    }
}
