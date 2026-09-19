package aq.project.test.services.deposit_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.WalletServiceDepositTransactionRequestDto;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.repositories.wallet.WalletRepository;
import aq.project.services.transaction.DepositTransactionService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
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

import static aq.project._utils.TransactionEntities.getValidDepositTransaction;
import static aq.project._utils.TransactionRequests.getValidDepositTransactionRequest;
import static aq.project._utils.WalletEntities.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleTransactionRequestIntegrationTest {

    private static AdminClient adminClient;

    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;
    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private DepositTransactionRepository depositTransactionRepository;

    @Autowired
    private DepositTransactionService depositTransactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKafkaContainerProperties(registry, KAFKA_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @BeforeAll
    public static void setUpKafkaTopics() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());

        NewTopic depositTransactionResponseTopic = new NewTopic("wallet_service_create_deposit_transaction_response", 3, (short) 1);
        NewTopic depositTransactionResponseExceptionsTopic = new NewTopic("wallet_service_create_deposit_transaction_response_exceptions", 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(List.of(
                depositTransactionResponseTopic,
                depositTransactionResponseExceptionsTopic
        ));
    }

    @AfterAll
    public static void tearDownKafkaTopics() {
        adminClient.close();
    }

    @AfterEach
    public void tearDownRepositories() {
        walletRepository.deleteAll();
        depositTransactionRepository.deleteAll();
    }

    @Test
    public void successHandleTransactionRequest() {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWallet(walletId);
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnDuplicateDepositTransaction() {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWallet(walletId);
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);
        DepositTransaction transaction = getValidDepositTransaction(transactionId);

        walletRepository.save(wallet);
        depositTransactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnWalletNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnWalletBlocked() {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWalletBlocked(walletId);
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnWalletCreditCardExpired() {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        CreditCard creditCard = getInvalidCreditCardExpired();

        Wallet wallet = getValidWallet(walletId);
        wallet.setCreditCard(creditCard);

        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));
    }
}
