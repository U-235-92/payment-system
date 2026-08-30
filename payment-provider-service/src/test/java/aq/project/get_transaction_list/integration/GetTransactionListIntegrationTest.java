package aq.project.get_transaction_list.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TransactionService;
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

import java.time.OffsetDateTime;

import static aq.project._utils.Entities.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionListIntegrationTest {

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
    public void successGetTransactionListIntegrationTest() {
//        Arrange
        Transaction transaction = getValidPendingTransaction();
        Merchant merchant = getValidMerchant();

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService
                .getTransactionList(OffsetDateTime.now(), OffsetDateTime.now().plusHours(1L), merchant.getId()));
    }

    @Test
    public void failGetTransactionListOnInvalidParametersIntegrationTest() {
//        Arrange
        String merchantId = getInvalidMerchantId();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .getTransactionList(null, OffsetDateTime.now().plusHours(1L), merchantId));
    }
}
