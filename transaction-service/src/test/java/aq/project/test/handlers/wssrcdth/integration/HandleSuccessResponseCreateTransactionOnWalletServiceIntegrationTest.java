package aq.project.test.handlers.wssrcdth.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceDepositTransactionSuccessResponseDto;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.utils.handlers.wallet_service.response.deposit_transaction.WalletServiceSuccessResponseCreateDepositTransactionHandler;
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
import static aq.project._utils.entities.wallet_service.WalletServiceSuccessResponseCreateDepositTransactionHandlerEntities.getInvalidWalletServiceDepositTransactionSuccessResponseDto;
import static aq.project._utils.entities.wallet_service.WalletServiceSuccessResponseCreateDepositTransactionHandlerEntities.getValidWalletServiceDepositTransactionSuccessResponseDto;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleSuccessResponseCreateTransactionOnWalletServiceIntegrationTest {

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
    private WalletServiceSuccessResponseCreateDepositTransactionHandler walletServiceSuccessResponseCreateDepositTransactionHandler;

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

        NewTopic walletServiceFailDepositTransactionRequestTopic = new NewTopic(
                "wallet_service_fail_deposit_transaction_request", 3, (short) 1);
        NewTopic paymentProviderServiceFailTransactionRequestTopic = new NewTopic(
                "payment_provider_service_fail_transaction_request", 3, (short) 1);
        NewTopic walletServiceCancelDepositTransactionRequestTopic = new NewTopic(
                "payment_provider_service_cancel_transaction_request", 3, (short) 1);
        NewTopic paymentProviderServiceCancelTransactionRequestTopic = new NewTopic(
                "wallet_service_cancel_deposit_transaction_request", 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(List.of(
                walletServiceFailDepositTransactionRequestTopic,
                paymentProviderServiceFailTransactionRequestTopic,
                walletServiceCancelDepositTransactionRequestTopic,
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
    }

    @Test
    public void successHandleSuccessResponseCreateTransactionOnWalletService() {
//        Arrange
        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(null);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.getTransactionMetadata().setId(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

        UUID transactionId = savedTransactionServiceDepositTransaction.getId();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));

        TransactionServiceDepositTransaction processedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.findById(transactionId).get();

        Assertions.assertEquals(TransactionStatus.COMPLETED, processedTransactionServiceDepositTransaction.getStatus());
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenReceivedInvalidWalletServiceDepositTransactionSuccessResponseDto() {
//        Arrange
        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(null);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.getTransactionMetadata().setId(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

        UUID transactionId = savedTransactionServiceDepositTransaction.getId();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getInvalidWalletServiceDepositTransactionSuccessResponseDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletServiceSuccessResponseCreateDepositTransactionHandler
                    .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));

        TransactionServiceDepositTransaction processedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.findById(transactionId).get();

        Assertions.assertEquals(TransactionStatus.PENDING, processedTransactionServiceDepositTransaction.getStatus());
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenReceivedNullWalletServiceDepositTransactionSuccessResponseDto() {
//        Arrange
        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(null);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.getTransactionMetadata().setId(null);

        TransactionServiceDepositTransaction savedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

        UUID transactionId = savedTransactionServiceDepositTransaction.getId();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletServiceSuccessResponseCreateDepositTransactionHandler
                        .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));

        TransactionServiceDepositTransaction processedTransactionServiceDepositTransaction = transactionServiceDepositTransactionRepository.findById(transactionId).get();

        Assertions.assertEquals(TransactionStatus.PENDING, processedTransactionServiceDepositTransaction.getStatus());
    }
}
