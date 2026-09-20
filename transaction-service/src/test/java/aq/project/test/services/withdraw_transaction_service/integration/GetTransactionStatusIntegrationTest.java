package aq.project.test.services.withdraw_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.services.WithdrawTransactionService;
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

import java.util.UUID;

import static aq.project._utils.entities.transaction_service.WithdrawTransactionServiceEntities.getValidTransactionServiceWithdrawTransaction;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionStatusIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    @Autowired
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Autowired
    private WithdrawTransactionService transactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void cleanRepositories() {
        transactionServiceWithdrawTransactionRepository.deleteAll();
        paymentProviderServiceTransactionRequestRepository.deleteAll();
    }

    @Test
    public void successGetTransactionStatus() {
//        Arrange
        TransactionServiceWithdrawTransaction transaction = getValidTransactionServiceWithdrawTransaction();
        transaction.setId(null);
        transaction.getTransactionMetadata().setId(null);
        transaction.getTransactionMetadata().setTimestamp(null);

        transactionServiceWithdrawTransactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.getTransactionStatus(transaction.getId()));
    }

    @Test
    public void failGetTransactionStatusOnEntityNotFoundException() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void successGetTransactionStatusOnNullTransactionId() {
//        Arrange
        UUID transactionId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.getTransactionStatus(transactionId));
    }
}
