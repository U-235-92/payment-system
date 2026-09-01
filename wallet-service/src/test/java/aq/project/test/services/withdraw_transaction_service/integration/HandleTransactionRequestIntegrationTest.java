package aq.project.test.services.withdraw_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.messages.requests.WithdrawTransactionRequest;
import aq.project.repositories.transaction.WithdrawTransactionRepository;
import aq.project.repositories.wallet.WalletRepository;
import aq.project.services.transaction.WithdrawTransactionService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static aq.project._utils.TransactionEntities.getValidWithdrawTransaction;
import static aq.project._utils.TransactionRequests.getInvalidWithdrawTransactionRequest;
import static aq.project._utils.TransactionRequests.getValidWithdrawTransactionRequest;
import static aq.project._utils.WalletEntities.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleTransactionRequestIntegrationTest {

    private static final String KAFKA_TEST_CONTAINER_TOPIC = "withdraw_transaction_response";

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
    private WithdrawTransactionRepository withdrawTransactionRepository;

    @Autowired
    private WithdrawTransactionService withdrawTransactionService;

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

        NewTopic walletOperationRequestTopic = new NewTopic(KAFKA_TEST_CONTAINER_TOPIC, 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(Collections.singleton(walletOperationRequestTopic));
    }

    @AfterAll
    public static void tearDownKafkaTopics() {
        adminClient.close();
    }

    @AfterEach
    public void tearDownRepositories() {
        walletRepository.deleteAll();
        withdrawTransactionRepository.deleteAll();
    }

    @Test
    public void successHandleTransactionRequest() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWallet(walletId);
        WithdrawTransactionRequest request = getValidWithdrawTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

        // Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnDuplicateWithdrawTransaction() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWallet(walletId);
        WithdrawTransactionRequest request = getValidWithdrawTransactionRequest(transactionId, walletId);
        WithdrawTransaction transaction = getValidWithdrawTransaction(transactionId);

        walletRepository.save(wallet);
        withdrawTransactionRepository.save(transaction);

        // Assert
        Assertions.assertThrows(DuplicateTransactionHandleException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnWalletNotFound() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        WithdrawTransactionRequest request = getValidWithdrawTransactionRequest(transactionId, walletId);

        // Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnWalletBlocked() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWalletBlocked(walletId);
        WithdrawTransactionRequest request = getValidWithdrawTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnWalletCreditCardExpired() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        CreditCard expiredCard = getInvalidCreditCardExpired();

        Wallet wallet = getValidWallet(walletId);
        wallet.setCreditCard(expiredCard);

        WithdrawTransactionRequest request = getValidWithdrawTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnInsufficientBalance() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWallet(walletId);
        // Устанавливаем баланс меньше суммы запроса (50.75)
        wallet.getCreditCard().setBalance(new BigDecimal("10.00"));

        WithdrawTransactionRequest request = getValidWithdrawTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnInvalidWithdrawTransactionRequest() {
        // Arrange & Act
        WithdrawTransactionRequest request = getInvalidWithdrawTransactionRequest();

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnNullWithdrawTransactionRequest() {
        // Arrange & Act
        WithdrawTransactionRequest request = null;

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));
    }
}