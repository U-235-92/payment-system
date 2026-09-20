package aq.project.test.services.withdraw_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.services.WithdrawTransactionService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
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
public class GetTransactionStatusResilenceIntegrationTest {

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
    public void failGetTransactionStatusOnRateLimitExceeded() {
//        Arrange
        TransactionServiceWithdrawTransaction transaction = getValidTransactionServiceWithdrawTransaction();
        transaction.setId(null);
        transaction.getTransactionMetadata().setId(null);
        transaction.getTransactionMetadata().setTimestamp(null);

        TransactionServiceWithdrawTransaction savedTransaction = transactionServiceWithdrawTransactionRepository.save(transaction);

        UUID transactionId = savedTransaction.getId();

//        The rate limit exceed [limit-for-period] property of application-test.yaml settings
//        of resilience4j [get-transfer-transaction-status-bulkhead]. Notice that settings uses ONLY
//        for check fallback logic call and MUST NOT override here.
        final int RATE_LIMIT = 50;

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1)
                new Thread(() -> transactionService.getTransactionStatus(transactionId)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> transactionService.getTransactionStatus(transactionId));
        }
}

@Test
public void failGetTransactionStatusOnBulkheadExceeded() {
//        Arrange
        TransactionServiceWithdrawTransaction transaction = getValidTransactionServiceWithdrawTransaction();
        transaction.setId(null);
        transaction.getTransactionMetadata().setId(null);
        transaction.getTransactionMetadata().setTimestamp(null);

        TransactionServiceWithdrawTransaction savedTransaction = transactionServiceWithdrawTransactionRepository.save(transaction);

        UUID transactionId = savedTransaction.getId();

//        The rate limit exceed [max-concurrent-calls] property of application-test.yaml settings
//        of resilience4j [get-transfer-transaction-status-bulkhead]. Notice that settings uses ONLY
//        for check fallback logic call and MUST NOT override here.
        final int BULKHEAD_LIMIT = 50;

//        Act & Assert
        for(int i = 0; i < BULKHEAD_LIMIT; i++) {
            if(i < BULKHEAD_LIMIT - 1)
                new Thread(() -> transactionService.getTransactionStatus(transactionId)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> transactionService.getTransactionStatus(transactionId));
        }
    }
}
