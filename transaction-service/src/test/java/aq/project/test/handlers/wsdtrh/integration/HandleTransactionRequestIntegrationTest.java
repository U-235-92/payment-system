package aq.project.test.handlers.wsdtrh.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.utils.handlers.wallet_service.request.WalletServiceDepositTransactionRequestHandler;
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

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;
import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getInvalidWalletServiceDepositTransactionRequest;
import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidWalletServiceDepositTransactionRequest;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleTransactionRequestIntegrationTest {

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
    private WalletServiceDepositTransactionRequestHandler walletServiceDepositTransactionRequestHandler;

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

        NewTopic walletServiceDepositTransactionRequestTopic = new NewTopic(
                "wallet_service_deposit_transaction_request", 3, (short) 1);
        NewTopic paymentProviderServiceFailTransactionRequestTopic = new NewTopic(
                "payment_provider_service_fail_transaction_request", 3, (short) 1);
        NewTopic paymentProviderServiceCancelTransactionRequestTopic = new NewTopic(
                "payment_provider_service_cancel_transaction_request", 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(List.of(
                walletServiceDepositTransactionRequestTopic,
                paymentProviderServiceFailTransactionRequestTopic,
                paymentProviderServiceCancelTransactionRequestTopic
        ));
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
    public void successHandleTransactionRequest() {
//        Arrange
        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.setId(null);
        transactionServiceDepositTransaction.getTransactionMetadata().setId(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

        UUID savedTransactionServiceDepositTransactionId = savedTransactionServiceDepositTransaction.getId();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.getTransactionRequestMetadata().setProcessed(false);
        walletServiceDepositTransactionRequest.setTransactionId(savedTransactionServiceDepositTransactionId);
        walletServiceDepositTransactionRequest.getTransactionRequestMetadata().setId(null);

        walletServiceDepositTransactionRequestRepository.save(walletServiceDepositTransactionRequest);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));

        TransactionServiceDepositTransaction processedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.findById(savedTransactionServiceDepositTransactionId).get();
        WalletServiceDepositTransactionRequest processedWalletServiceDepositTransactionRequest = walletServiceDepositTransactionRequestRepository.findById(savedTransactionServiceDepositTransactionId).get();

        Assertions.assertEquals(TransactionStatus.PENDING, processedTransactionServiceDepositTransaction.getStatus());
        Assertions.assertTrue(processedWalletServiceDepositTransactionRequest.getTransactionRequestMetadata().isProcessed());
    }

    @Test
    public void failHandleTransactionRequestWhenTransactionServiceDepositTransactionIsFailedStatus() {
//        Arrange
        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);
        transactionServiceDepositTransaction.setId(null);
        transactionServiceDepositTransaction.getTransactionMetadata().setId(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

        UUID savedTransactionServiceDepositTransactionId = savedTransactionServiceDepositTransaction.getId();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.getTransactionRequestMetadata().setProcessed(false);
        walletServiceDepositTransactionRequest.setTransactionId(savedTransactionServiceDepositTransactionId);
        walletServiceDepositTransactionRequest.getTransactionRequestMetadata().setId(null);

        walletServiceDepositTransactionRequestRepository.save(walletServiceDepositTransactionRequest);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));

        TransactionServiceDepositTransaction processedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.findById(savedTransactionServiceDepositTransactionId).get();
        WalletServiceDepositTransactionRequest processedWalletServiceDepositTransactionRequest = walletServiceDepositTransactionRequestRepository.findById(savedTransactionServiceDepositTransactionId).get();

        Assertions.assertEquals(TransactionStatus.FAILED, processedTransactionServiceDepositTransaction.getStatus());
        Assertions.assertTrue(processedWalletServiceDepositTransactionRequest.getTransactionRequestMetadata().isProcessed());
    }

    @Test
    public void failHandleTransactionRequestOnInvalidWalletServiceDepositTransactionRequest() {
//        Arrange
        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getInvalidWalletServiceDepositTransactionRequest();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));
    }

    @Test
    public void failHandleTransactionRequestOnNullWalletServiceDepositTransactionRequest() {
//        Arrange
        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));
    }
}
