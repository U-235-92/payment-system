package aq.project.test.services.transfer_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.transaction.TransferTransactionRepository;
import aq.project.services.transaction.TransferTransactionService;
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

import static aq.project._utils.TransactionEntities.getValidTransferTransaction;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionStatusIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private TransferTransactionService transferTransactionService;

    @Autowired
    private TransferTransactionRepository transferTransactionRepository;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void tearDownRepository() {
        transferTransactionRepository.deleteAll();
    }

    @Test
    public void successGetTransactionStatus() {
        // Arrange & Act
        TransferTransaction transaction = getValidTransferTransaction();
        UUID transactionId = transaction.getId();

        transferTransactionRepository.save(transaction);

        // Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOnTransactionNotFound() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();

        // Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transferTransactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOnNullTransactionId() {
        // Arrange & Act
        UUID transactionId = null;

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transferTransactionService.getTransactionStatus(transactionId));
    }
}