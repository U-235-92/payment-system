package aq.project.test.services.transfer_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceTransferTransactionRequestRepository;
import aq.project.services.TransferTransactionService;
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

import static aq.project._utils.entities.transaction_service.TransferTransactionServiceEntities.getValidTransactionServiceTransferTransaction;

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
    private TransactionServiceTransferTransactionRepository transactionServiceTransferTransactionRepository;
    @Autowired
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;
    @Autowired
    private WalletServiceTransferTransactionRequestRepository walletServiceTransferTransactionRequestRepository;

    @Autowired
    private TransferTransactionService transferTransactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void cleanRepositories() {
        transactionServiceTransferTransactionRepository.deleteAll();
        paymentProviderServiceTransactionRequestRepository.deleteAll();
        walletServiceTransferTransactionRequestRepository.deleteAll();
    }

    @Test
    public void successFailTransaction() {
//        Arrange
        TransactionServiceTransferTransaction newTransactionServiceTransferTransaction = getValidTransactionServiceTransferTransaction();
        newTransactionServiceTransferTransaction.setId(null);
        newTransactionServiceTransferTransaction.setStatus(TransactionStatus.PENDING);
        newTransactionServiceTransferTransaction.getTransactionMetadata().setId(null);

        TransactionServiceTransferTransaction savedTransactionServiceTransferTransaction = transactionServiceTransferTransactionRepository
                .save(newTransactionServiceTransferTransaction);

        UUID transactionId = savedTransactionServiceTransferTransaction.getId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.failTransaction(transactionId));
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
                new Thread(() -> transferTransactionService.failTransaction(transactionId)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> transferTransactionService.failTransaction(transactionId));
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
                new Thread(() -> transferTransactionService.failTransaction(transactionId)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> transferTransactionService.failTransaction(transactionId));
        }
    }

    private List<UUID> getTransactionIdList(int size) {
        List<UUID> transactionIdList = new ArrayList<>();

        for(int i = 0; i < size; i++) {
            TransactionServiceTransferTransaction newTransactionServiceTransferTransaction = getValidTransactionServiceTransferTransaction();
            newTransactionServiceTransferTransaction.setId(null);
            newTransactionServiceTransferTransaction.setStatus(TransactionStatus.PENDING);
            newTransactionServiceTransferTransaction.getTransactionMetadata().setId(null);

            TransactionServiceTransferTransaction savedTransactionServiceTransferTransaction = transactionServiceTransferTransactionRepository
                    .save(newTransactionServiceTransferTransaction);

            UUID transactionId = savedTransactionServiceTransferTransaction.getId();

            transactionIdList.add(transactionId);
        }
        return transactionIdList;
    }
}
