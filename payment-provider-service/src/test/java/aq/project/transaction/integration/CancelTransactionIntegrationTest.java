package aq.project.transaction.integration;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TransactionService;
import aq.project._utils.Containers;
import aq.project._utils.ContainerPropertiesConfigurer;
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

import static aq.project._utils.Entities.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CancelTransactionIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void tearDownRepositories() {
        merchantRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    public void successCancelTransactionIntegrationTest() {
//        Arrange
        Transaction transaction = getValidTransaction();
        Merchant merchant = getValidMerchant();
        TransactionStatus transactionStatus = TransactionStatus.FAILED;

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.cancelTransaction(transaction.getId(), merchant.getId(), transactionStatus));
    }

    @Test
    public void failCancelTransactionOnInvalidParametersIntegrationTest() {
//        Arrange
        String merchantId = getInvalidMerchantId();
        TransactionStatus transactionStatus = TransactionStatus.FAILED;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.cancelTransaction(null, merchantId, transactionStatus));
    }
}