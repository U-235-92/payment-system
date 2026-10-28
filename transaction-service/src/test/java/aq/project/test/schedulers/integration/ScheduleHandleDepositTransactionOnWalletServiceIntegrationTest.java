package aq.project.test.schedulers.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.utils.handlers.wallet_service.request.WalletServiceDepositTransactionRequestHandler;
import aq.project.utils.schedulers.WalletServiceCreateTransactionScheduler;
import dasniko.testcontainers.keycloak.KeycloakContainer;
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

import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.*;
import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidWalletServiceDepositTransactionRequest;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScheduleHandleDepositTransactionOnWalletServiceIntegrationTest {

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

    @Autowired
    private WalletServiceCreateTransactionScheduler walletServiceCreateTransactionScheduler;

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

        NewTopic walletServiceCreateDepositTransactionRequestTopic = new NewTopic(
                "wallet_service_create_deposit_transaction_request", 3, (short) 1);
        NewTopic paymentProviderServiceFailTransactionRequestTopic = new NewTopic(
                "payment_provider_service_fail_transaction_request", 3, (short) 1);
        NewTopic paymentProviderServiceCancelTransactionRequestTopic = new NewTopic(
                "payment_provider_service_cancel_transaction_request", 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(List.of(
                walletServiceCreateDepositTransactionRequestTopic,
                paymentProviderServiceFailTransactionRequestTopic,
                paymentProviderServiceCancelTransactionRequestTopic));
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
    public void successScheduleHandleCreateDepositTransactionOnWalletService() {
//        Arrange
        TransactionServiceDepositTransaction newTransactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        newTransactionServiceDepositTransaction.setId(null);
        newTransactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        newTransactionServiceDepositTransaction.getTransactionMetadata().setId(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository
                .save(newTransactionServiceDepositTransaction);

        UUID savedTransactionId = savedTransactionServiceDepositTransaction.getId();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setId(null);
        walletServiceDepositTransactionRequest.setStatus(TransactionStatus.PENDING);
        walletServiceDepositTransactionRequest.setTransactionId(savedTransactionId);
        walletServiceDepositTransactionRequest.getTransactionRequestMetadata().setId(null);

        walletServiceDepositTransactionRequestRepository.save(walletServiceDepositTransactionRequest);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleDepositTransactionOnWalletService());
    }

    @Test
    public void successScheduleHandleFailDepositTransactionOnWalletService() {
//        Arrange
        TransactionServiceDepositTransaction newTransactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        newTransactionServiceDepositTransaction.setId(null);
        newTransactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        newTransactionServiceDepositTransaction.getTransactionMetadata().setId(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository
                .save(newTransactionServiceDepositTransaction);

        UUID savedTransactionId = savedTransactionServiceDepositTransaction.getId();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setId(null);
        walletServiceDepositTransactionRequest.setStatus(TransactionStatus.FAILED);
        walletServiceDepositTransactionRequest.setTransactionId(savedTransactionId);
        walletServiceDepositTransactionRequest.getTransactionRequestMetadata().setId(null);

        walletServiceDepositTransactionRequestRepository.save(walletServiceDepositTransactionRequest);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleDepositTransactionOnWalletService());
    }
}
