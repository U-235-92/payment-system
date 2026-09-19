package aq.project.handlers.create_transaction_handler.integration;


import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceCreateTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.DtoConstraintsException;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.CreateTransactionHandler;
import jakarta.validation.ConstraintViolationException;
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

import java.math.BigDecimal;
import java.util.UUID;

import static aq.project._utils.entities.CreateTransactionHandlerEntities.*;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleCreateTransactionRequestIntegrationTest {

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
    public void successHandleCreateTransactionRequest() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getValidPaymentProviderServiceCreateTransactionRequestDto();;
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);
        paymentProviderServiceCreateTransactionRequestDto.setMerchantId(merchantId);

        Merchant merchant = getValidMerchant();
        merchant.setId(merchantId);
        merchant.setCreatedAt(null);
        merchant.setUpdatedAt(null);

        merchantRepository.save(merchant);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> createTransactionHandler
                .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));

        Transaction savedTransaction = transactionRepository.findAll().iterator().next();

        Assertions.assertNotNull(savedTransaction);
        Assertions.assertEquals(TransactionStatus.PENDING, savedTransaction.getStatus());
    }

    @Test
    public void failHandleCreateTransactionRequestOnDtoConstraintsException() {
//        Arrange
        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getInvalidPaymentProviderServiceCreateTransactionRequestDto();;
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> createTransactionHandler
                        .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));
    }

    @Test
    public void failHandleCreateTransactionRequestOnConstraintViolationException() {
//        Arrange
        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getValidPaymentProviderServiceCreateTransactionRequestDto();
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);
        paymentProviderServiceCreateTransactionRequestDto.setAmount(BigDecimal.valueOf(-100.00));

//        Act & Assert
        Assertions.assertThrows(DtoConstraintsException.class,
                () -> createTransactionHandler
                        .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));
    }

    @Test
    public void failHandleCreateTransactionRequestOnTransactionAlreadyExists() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        Merchant merchant = getValidMerchant();
        merchant.setId(merchantId);
        merchant.setCreatedAt(null);
        merchant.setUpdatedAt(null);

        merchantRepository.save(merchant);

        Transaction transaction = getValidTransaction();
        transaction.setMerchant(merchant);

        transactionRepository.save(transaction);

        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getValidPaymentProviderServiceCreateTransactionRequestDto();
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);
        paymentProviderServiceCreateTransactionRequestDto.setTransactionId(transaction.getId());

//        Act & Assert
        Assertions.assertThrows(EntityAlreadyExistsException.class,
                () -> createTransactionHandler
                        .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));
    }

    @Test
    public void failHandleCreateTransactionRequestOnMerchantNotFound() {
//        Arrange
        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getValidPaymentProviderServiceCreateTransactionRequestDto();
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> createTransactionHandler
                        .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));
    }
}
