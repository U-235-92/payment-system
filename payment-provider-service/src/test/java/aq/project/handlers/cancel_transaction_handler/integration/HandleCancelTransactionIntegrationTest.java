package aq.project.handlers.cancel_transaction_handler.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PaymentProviderServiceCancelTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.CancelTransactionHandler;
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

import static aq.project._utils.entities.CancelTransactionHandlerEntities.*;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleCancelTransactionIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private CancelTransactionHandler cancelTransactionHandler;

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
    public void successHandleCancelTransaction() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceCancelTransactionRequestDto requestDto =
                getValidPaymentProviderServiceCancelTransactionRequestDto();
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
        Assertions.assertDoesNotThrow(() -> cancelTransactionHandler.handleCancelTransaction(requestDto));

        Transaction savedTransaction = transactionRepository.findAll().iterator().next();

        Assertions.assertNotNull(savedTransaction);
        Assertions.assertEquals(TransactionStatus.MARKED_CANCELED, savedTransaction.getStatus());
    }

    @Test
    public void failHandleCancelTransactionOnMerchantDoesntExist() {
//        Arrange
        PaymentProviderServiceCancelTransactionRequestDto requestDto =
                getValidPaymentProviderServiceCancelTransactionRequestDto();
        requestDto.setMerchantId(UUID.randomUUID().toString());

        Merchant merchant = getValidMerchant();

        merchantRepository.save(merchant);

        Transaction transaction = getValidTransaction();
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setMerchant(merchant);

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> cancelTransactionHandler.handleCancelTransaction(requestDto));
    }

    @Test
    public void failHandleCancelTransactionOnForeignMerchantTransactionException() {
//        Arrange
        Merchant merchantA = getValidMerchant();
        merchantA.setId("merchant-A");

        Merchant merchantB = getValidMerchant();
        merchantB.setId("merchant-B");

        merchantRepository.save(merchantA);
        merchantRepository.save(merchantB);

        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceCancelTransactionRequestDto requestDto =
                getValidPaymentProviderServiceCancelTransactionRequestDto();
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
                () -> cancelTransactionHandler.handleCancelTransaction(requestDto));
    }

    @Test
    public void failHandleCancelTransactionOnTransactionInNotPendingOrCompletedStatus() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceCancelTransactionRequestDto requestDto =
                getValidPaymentProviderServiceCancelTransactionRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setTransactionId(transactionId);

        Merchant merchant = getValidMerchant();
        merchant.setId(merchantId);

        merchantRepository.save(merchant);

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setMerchant(merchant);

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(ProhibitedOperationException.class,
                () -> cancelTransactionHandler.handleCancelTransaction(requestDto));

        Transaction savedTransaction = transactionRepository.findAll().iterator().next();

        Assertions.assertNotNull(savedTransaction);
        Assertions.assertEquals(TransactionStatus.FAILED, savedTransaction.getStatus());
    }
}
