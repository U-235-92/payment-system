package aq.project.test.services.deposit_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.services.DepositTransactionService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FailTransactionIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    @Autowired
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;
    @Autowired
    private WalletServiceDepositTransactionRequestRepository walletServiceDepositTransactionRequestRepository;

    @Autowired
    private DepositTransactionService depositTransactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void cleanRepositories() {
        transactionServiceDepositTransactionRepository.deleteAll();
        paymentProviderServiceTransactionRequestRepository.deleteAll();
        walletServiceDepositTransactionRequestRepository.deleteAll();
    }

    @Test
    public void successFailTransaction() {
//        Arrange
        TransactionServiceDepositTransaction newTransactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        newTransactionServiceDepositTransaction.setId(null);
        newTransactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        newTransactionServiceDepositTransaction.getTransactionMetadata().setId(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository
                .save(newTransactionServiceDepositTransaction);

        UUID transactionId = savedTransactionServiceDepositTransaction.getId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.failTransaction(transactionId));
    }

    @Test
    public void failFailTransactionOnRateLimitExceeded() {
//        Arrange
        final int RATE_LIMIT = 50;

        List<UUID> transactionIdList = getTransactionIdList(RATE_LIMIT);

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            UUID transactionId = transactionIdList.get(i);

            if(i < RATE_LIMIT - 1)
                new Thread(() -> depositTransactionService.failTransaction(transactionId)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> depositTransactionService.failTransaction(transactionId));
        }
    }

    @Test
    public void failFailTransactionOnBulkheadExceeded() {
//        Arrange
        final int BULKHEAD_LIMIT = 50;

        List<UUID> transactionIdList = getTransactionIdList(BULKHEAD_LIMIT);

//        Act & Assert
        for(int i = 0; i < BULKHEAD_LIMIT; i++) {
            UUID transactionId = transactionIdList.get(i);

            if(i < BULKHEAD_LIMIT - 1)
                new Thread(() -> depositTransactionService.failTransaction(transactionId)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> depositTransactionService.failTransaction(transactionId));
        }
    }

    private List<UUID> getTransactionIdList(int size) {
        List<UUID> transactionIdList = new ArrayList<>();

        for(int i = 0; i < size; i++) {
            TransactionServiceDepositTransaction newTransactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
            newTransactionServiceDepositTransaction.setId(null);
            newTransactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
            newTransactionServiceDepositTransaction.getTransactionMetadata().setId(null);

            TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository
                    .save(newTransactionServiceDepositTransaction);

            UUID transactionId = savedTransactionServiceDepositTransaction.getId();

            transactionIdList.add(transactionId);
        }
        return transactionIdList;
    }
}
