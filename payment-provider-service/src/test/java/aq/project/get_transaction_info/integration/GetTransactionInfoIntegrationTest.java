package aq.project.get_transaction_info.integration;

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
public class GetTransactionInfoIntegrationTest {

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
    public void successGetTransactionInfoIntegrationTest() {
//        Arrange
        Transaction transaction = getValidPendingTransaction();
        Merchant merchant = getValidMerchant();

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.getTransactionInfo(transaction.getId(), merchant.getId()));
    }

    @Test
    public void failGetTransactionInfoOnInvalidParametersIntegrationTest() {
//        Arrange
        String merchantId = getInvalidMerchantId();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.getTransactionInfo(null, merchantId));
    }
}