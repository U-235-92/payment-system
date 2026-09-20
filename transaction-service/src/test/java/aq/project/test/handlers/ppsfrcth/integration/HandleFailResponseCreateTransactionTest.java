package aq.project.test.handlers.ppsfrcth.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceErrorHandleTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.utils.handlers.payment_provider_service.response.PaymentProviderServiceFailResponseCreateTransactionHandler;
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

import static aq.project._utils.entities.payment_provider_service.PaymentProviderServiceEntities.getInvalidPaymentProviderServiceErrorHandleTransactionDto;
import static aq.project._utils.entities.payment_provider_service.PaymentProviderServiceEntities.getValidPaymentProviderServiceErrorHandleTransactionDto;
import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleFailResponseCreateTransactionTest {

    private static final String KAFKA_TEST_CONTAINER_TOPIC = "payment_provider_service_create_transaction_request_exceptions";

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
    private PaymentProviderServiceFailResponseCreateTransactionHandler paymentProviderServiceFailResponseCreateTransactionHandler;

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
    }

    @Test
    public void successHandleFailResponseCreateTransactionOnPaymentProviderService() {
//        Arrange
        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(null);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.getTransactionMetadata().setId(null);
        transactionServiceDepositTransaction.getTransactionMetadata().setTimestamp(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

        UUID savedTransactionServiceDepositTransactionId = savedTransactionServiceDepositTransaction.getId();

        PaymentProviderServiceErrorHandleTransactionDto errorHandleTransactionDto = getValidPaymentProviderServiceErrorHandleTransactionDto();
        errorHandleTransactionDto.setTransactionId(savedTransactionServiceDepositTransactionId);
        errorHandleTransactionDto.setOperation(Operation.DEPOSIT);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceFailResponseCreateTransactionHandler.handleFailResponseCreateTransactionOnPaymentProviderService(errorHandleTransactionDto));

        TransactionServiceDepositTransaction updatedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.findById(savedTransactionServiceDepositTransactionId).get();

        Assertions.assertEquals(updatedTransactionServiceDepositTransaction.getStatus().getValue(), TransactionStatus.FAILED.getValue());
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnPaymentProviderServiceWhenReceivedInvalidDto() {
//        Arrange
        PaymentProviderServiceErrorHandleTransactionDto errorHandleTransactionDto = getInvalidPaymentProviderServiceErrorHandleTransactionDto();
        errorHandleTransactionDto.setOperation(Operation.DEPOSIT);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentProviderServiceFailResponseCreateTransactionHandler.handleFailResponseCreateTransactionOnPaymentProviderService(errorHandleTransactionDto));
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnPaymentProviderServiceWhenReceivedNullDto() {
//        Arrange
        PaymentProviderServiceErrorHandleTransactionDto errorHandleTransactionDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentProviderServiceFailResponseCreateTransactionHandler.handleFailResponseCreateTransactionOnPaymentProviderService(errorHandleTransactionDto));
    }
}
