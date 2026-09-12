package aq.project.test.services.deposit_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.services.transaction.DepositTransactionService;
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

import static aq.project._utils.TransactionEntities.*;

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
    private DepositTransactionService depositTransactionService;

    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void tearDownRepository() {
        depositTransactionRepository.deleteAll();
    }

    @Test
    public void successGetTransactionStatus() {
//        Arrange
        DepositTransaction transaction = getValidDepositTransaction();
        UUID transactionID = transaction.getId();

        depositTransactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.getTransactionStatus(transactionID));
    }

    @Test
    public void failGetTransactionStatusOnTransactionNotFound() {
//        Arrange
        UUID transactionID = UUID.randomUUID();

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> depositTransactionService.getTransactionStatus(transactionID));
    }

    @Test
    public void failGetTransactionStatusOnNullTransactionId() {
//        Arrange
        UUID transactionID = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.getTransactionStatus(transactionID));
    }
}
