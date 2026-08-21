package aq.project.webhook.integration;

import aq.project.dto.TransactionStatus;
import aq.project.dto.TransactionStatusDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.repositories.WebhookRepository;
import aq.project.services.WebhookService;
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
public class UpdateTransactionStatusIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WebhookRepository webhookRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private WebhookService webhookService;

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void tearDownDb() {
        transactionRepository.deleteAll();
        webhookRepository.deleteAll();
        merchantRepository.deleteAll();
    }

    @Test
    public void successUpdateTransactionStatusIntegrationTest() {
//        Arrange
        Merchant merchant = getValidMerchant();
        Transaction transaction = getValidTransaction();
        TransactionStatusDto transactionStatusDto = getValidTransactionStatusDto();

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> webhookService.updateTransactionStatus(transactionStatusDto));

        Transaction updated = transactionRepository.findById(transaction.getId()).orElseThrow();
        Assertions.assertEquals(TransactionStatus.COMPLETED, updated.getStatus());

        long webhookCount = webhookRepository.count();
        Assertions.assertEquals(1, webhookCount);
    }

    @Test
    public void failUpdateTransactionStatusOnInvalidTransactionStatusDtoIntegrationTest() {
//        Arrange
        Merchant merchant = getValidMerchant();
        Transaction transaction = getValidTransaction();
        TransactionStatusDto transactionStatusDto = getInvalidTransactionStatusDto();

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> webhookService.updateTransactionStatus(transactionStatusDto));
    }
}
