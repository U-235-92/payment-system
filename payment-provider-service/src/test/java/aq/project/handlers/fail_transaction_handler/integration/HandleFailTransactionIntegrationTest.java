package aq.project.handlers.fail_transaction_handler.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PaymentProviderServiceFailTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.FailTransactionHandler;
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

import static aq.project._utils.entities.FailTransactionHandlerEntities.*;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleFailTransactionIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    
    @Autowired
    private FailTransactionHandler failTransactionHandler;

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
    public void successHandleFailTransaction() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceFailTransactionRequestDto requestDto =
                getValidPaymentProviderServiceFailTransactionRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setTransactionId(transactionId);

        Merchant merchant = getValidMerchant();
        merchant.setId(merchantId);

        merchantRepository.save(merchant);

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setMerchant(merchant);

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionHandler.handleFailTransaction(requestDto));

        Transaction savedTransaction = transactionRepository.findAll().iterator().next();

        Assertions.assertNotNull(savedTransaction);
        Assertions.assertEquals(TransactionStatus.MARKED_FAILED, savedTransaction.getStatus());
    }

    @Test
    public void failHandleFailTransactionOnMerchantDoesntExist() {
//        Arrange
        PaymentProviderServiceFailTransactionRequestDto requestDto =
                getValidPaymentProviderServiceFailTransactionRequestDto();
        requestDto.setMerchantId(UUID.randomUUID().toString());

        Merchant merchant = getValidMerchant();

        merchantRepository.save(merchant);

        Transaction transaction = getValidTransaction();
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setMerchant(merchant);

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> failTransactionHandler.handleFailTransaction(requestDto));
    }

    @Test
    public void failHandleFailTransactionOnForeignMerchantTransactionException() {
//        Arrange
        Merchant merchantA = getValidMerchant();
        merchantA.setId("merchant-A");

        Merchant merchantB = getValidMerchant();
        merchantB.setId("merchant-B");

        merchantRepository.save(merchantA);
        merchantRepository.save(merchantB);

        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceFailTransactionRequestDto requestDto =
                getValidPaymentProviderServiceFailTransactionRequestDto();
        requestDto.setTransactionId(transactionId);
        requestDto.setMerchantId(merchantA.getId());

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setMerchant(merchantB);

        transactionRepository.save(transaction);

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(ForeignMerchantTransactionException.class,
                () -> failTransactionHandler.handleFailTransaction(requestDto));
    }

    @Test
    public void failHandleFailTransactionOnTransactionInNotPendingOrCompletedStatus() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceFailTransactionRequestDto requestDto =
                getValidPaymentProviderServiceFailTransactionRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setTransactionId(transactionId);

        Merchant merchant = getValidMerchant();
        merchant.setId(merchantId);

        merchantRepository.save(merchant);

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);
        transaction.setStatus(TransactionStatus.CANCELED);
        transaction.setMerchant(merchant);

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(ProhibitedOperationException.class,
                () -> failTransactionHandler.handleFailTransaction(requestDto));

        Transaction savedTransaction = transactionRepository.findAll().iterator().next();

        Assertions.assertNotNull(savedTransaction);
        Assertions.assertEquals(TransactionStatus.CANCELED, savedTransaction.getStatus());
    }
}
