package aq.project.handlers.create_transaction_handler.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.Operation;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.CreateTransactionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static aq.project._utils.entities.CreateTransactionHandlerEntities.getValidMerchant;
import static aq.project._utils.entities.CreateTransactionHandlerEntities.getValidTransaction;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleScheduleCreateTransactionIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private CreateTransactionHandler createTransactionHandler;

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKafkaContainerProperties(registry, KAFKA_CONTAINER);
    }

    @AfterEach
    public void tearDownRepositories() {
        merchantRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    public void successHandleScheduleCreateTransaction() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        Merchant merchant = getValidMerchant();
        merchant.setId(merchantId);
        merchant.setCreatedAt(null);
        merchant.setUpdatedAt(null);

        merchantRepository.save(merchant);

        Transaction transaction = getValidTransaction();
        transaction.setOperation(Operation.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setMerchant(merchant);

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> createTransactionHandler.handleScheduleCreateTransaction(transaction));

        Transaction procesedTransaction = transactionRepository.findAll().iterator().next();

        Assertions.assertEquals(TransactionStatus.COMPLETED, procesedTransaction.getStatus());
    }
}
