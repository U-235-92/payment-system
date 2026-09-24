package aq.project.test.handlers.ppssrcth.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceSuccessHandleTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.utils.handlers.payment_provider_service.response.PaymentProviderServiceSuccessResponseCreateTransactionHandler;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static aq.project._utils.entities.payment_provider_service.PaymentProviderServiceEntities.getInvalidPaymentProviderServiceSuccessHandleTransactionDto;
import static aq.project._utils.entities.payment_provider_service.PaymentProviderServiceEntities.getValidPaymentProviderServiceSuccessHandleTransactionDto;
import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleSuccessResponseCreateTransactionIntegrationTest {

    private static final String KAFKA_TEST_CONTAINER_TOPIC = "payment_provider_service_create_transaction_request";

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;
    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;

    private static AdminClient adminClient;

    @Autowired
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    @Autowired
    private WalletServiceDepositTransactionRequestRepository walletServiceDepositTransactionRequestRepository;

    @Autowired
    private PaymentProviderServiceSuccessResponseCreateTransactionHandler paymentProviderServiceSuccessResponseCreateTransactionHandler;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKafkaContainerProperties(registry, KAFKA_CONTAINER);
    }

    @BeforeAll
    public static void createKafkaTopics() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());

        NewTopic walletOperationRequestTopic = new NewTopic(KAFKA_TEST_CONTAINER_TOPIC, 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(Collections.singleton(walletOperationRequestTopic));
    }

    @AfterAll
    public static void cleanKafkaTopics() {
        adminClient.close();
    }

    @AfterEach
    void cleanRepositories() {
        transactionServiceDepositTransactionRepository.deleteAll();
        walletServiceDepositTransactionRequestRepository.deleteAll();
    }

    @Test
    public void successHandleSuccessResponseCreateTransactionOnPaymentProviderService() {
//        Arrange
        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(null);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.getTransactionMetadata().setId(null);
        transactionServiceDepositTransaction.getTransactionMetadata().setTimestamp(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

        UUID savedTransactionServiceDepositTransactionId = savedTransactionServiceDepositTransaction.getId();

        PaymentProviderServiceSuccessHandleTransactionDto successHandleTransactionDto = getValidPaymentProviderServiceSuccessHandleTransactionDto();
        successHandleTransactionDto.setTransactionId(savedTransactionServiceDepositTransactionId);
        successHandleTransactionDto.setOperation(Operation.DEPOSIT);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceSuccessResponseCreateTransactionHandler
                .handleSuccessResponseCreateTransactionOnPaymentProviderService(successHandleTransactionDto));
        Assertions.assertTrue(walletServiceDepositTransactionRequestRepository.findById(savedTransactionServiceDepositTransactionId).isPresent());
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnPaymentProviderServiceWhenReceivedInvalidDto() {
//        Arrange
        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(null);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.getTransactionMetadata().setId(null);
        transactionServiceDepositTransaction.getTransactionMetadata().setTimestamp(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

        UUID savedTransactionServiceDepositTransactionId = savedTransactionServiceDepositTransaction.getId();

        PaymentProviderServiceSuccessHandleTransactionDto successHandleTransactionDto = getInvalidPaymentProviderServiceSuccessHandleTransactionDto();
        successHandleTransactionDto.setTransactionId(savedTransactionServiceDepositTransactionId);
        successHandleTransactionDto.setOperation(Operation.DEPOSIT);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentProviderServiceSuccessResponseCreateTransactionHandler
                        .handleSuccessResponseCreateTransactionOnPaymentProviderService(successHandleTransactionDto));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnPaymentProviderServiceWhenReceivedNullDto() {
//        Arrange
        PaymentProviderServiceSuccessHandleTransactionDto successHandleTransactionDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentProviderServiceSuccessResponseCreateTransactionHandler
                        .handleSuccessResponseCreateTransactionOnPaymentProviderService(successHandleTransactionDto));
    }
}
