package aq.project.test.schedulers.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceWithdrawTransactionRequestRepository;
import aq.project.utils.handlers.wallet_service.request.WalletServiceWithdrawTransactionRequestHandler;
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

import static aq.project._utils.entities.transaction_service.WithdrawTransactionServiceEntities.getValidTransactionServiceWithdrawTransaction;
import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidWalletServiceWithdrawTransactionRequest;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScheduleHandleWithdrawTransactionOnWalletServiceIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;
    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;

    private static AdminClient adminClient;

    @Autowired
    private TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    @Autowired
    private WalletServiceWithdrawTransactionRequestRepository walletServiceWithdrawTransactionRequestRepository;

    @Autowired
    private WalletServiceWithdrawTransactionRequestHandler walletServiceWithdrawTransactionRequestHandler;

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

        NewTopic walletServiceCreateWithdrawTransactionRequestTopic = new NewTopic(
                "wallet_service_create_withdraw_transaction_request", 3, (short) 1);
        NewTopic paymentProviderServiceFailTransactionRequestTopic = new NewTopic(
                "payment_provider_service_fail_transaction_request", 3, (short) 1);
        NewTopic paymentProviderServiceCancelTransactionRequestTopic = new NewTopic(
                "payment_provider_service_cancel_transaction_request", 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(List.of(
                walletServiceCreateWithdrawTransactionRequestTopic,
                paymentProviderServiceFailTransactionRequestTopic,
                paymentProviderServiceCancelTransactionRequestTopic));
    }

    @AfterAll
    public static void cleanKafkaTopics() {
        adminClient.close();
    }

    @AfterEach
    void cleanRepositories() {
        transactionServiceWithdrawTransactionRepository.deleteAll();
        walletServiceWithdrawTransactionRequestRepository.deleteAll();
    }

    @Test
    public void successScheduleHandleCreateWithdrawTransactionOnWalletService() {
//        Arrange
        TransactionServiceWithdrawTransaction newTransactionServiceWithdrawTransaction = getValidTransactionServiceWithdrawTransaction();
        newTransactionServiceWithdrawTransaction.setId(null);
        newTransactionServiceWithdrawTransaction.setStatus(TransactionStatus.PENDING);
        newTransactionServiceWithdrawTransaction.getTransactionMetadata().setId(null);

        TransactionServiceWithdrawTransaction savedTransactionServiceWithdrawTransaction = transactionServiceWithdrawTransactionRepository
                .save(newTransactionServiceWithdrawTransaction);

        UUID savedTransactionId = savedTransactionServiceWithdrawTransaction.getId();

        WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest = getValidWalletServiceWithdrawTransactionRequest();
        walletServiceWithdrawTransactionRequest.setId(null);
        walletServiceWithdrawTransactionRequest.setStatus(TransactionStatus.PENDING);
        walletServiceWithdrawTransactionRequest.setTransactionId(savedTransactionId);
        walletServiceWithdrawTransactionRequest.getTransactionRequestMetadata().setId(null);

        walletServiceWithdrawTransactionRequestRepository.save(walletServiceWithdrawTransactionRequest);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleWithdrawTransactionOnWalletService());
    }

    @Test
    public void successScheduleHandleFailWithdrawTransactionOnWalletService() {
//        Arrange
        TransactionServiceWithdrawTransaction newTransactionServiceWithdrawTransaction = getValidTransactionServiceWithdrawTransaction();
        newTransactionServiceWithdrawTransaction.setId(null);
        newTransactionServiceWithdrawTransaction.setStatus(TransactionStatus.PENDING);
        newTransactionServiceWithdrawTransaction.getTransactionMetadata().setId(null);

        TransactionServiceWithdrawTransaction savedTransactionServiceWithdrawTransaction = transactionServiceWithdrawTransactionRepository
                .save(newTransactionServiceWithdrawTransaction);

        UUID savedTransactionId = savedTransactionServiceWithdrawTransaction.getId();

        WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest = getValidWalletServiceWithdrawTransactionRequest();
        walletServiceWithdrawTransactionRequest.setId(null);
        walletServiceWithdrawTransactionRequest.setStatus(TransactionStatus.FAILED);
        walletServiceWithdrawTransactionRequest.setTransactionId(savedTransactionId);
        walletServiceWithdrawTransactionRequest.getTransactionRequestMetadata().setId(null);

        walletServiceWithdrawTransactionRequestRepository.save(walletServiceWithdrawTransactionRequest);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleWithdrawTransactionOnWalletService());
    }
}
