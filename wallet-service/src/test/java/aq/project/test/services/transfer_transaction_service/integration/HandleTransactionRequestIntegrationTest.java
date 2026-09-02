package aq.project.test.services.transfer_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.dto.TransferTransactionRequestDto;
import aq.project.repositories.transaction.TransferTransactionRepository;
import aq.project.repositories.wallet.WalletRepository;
import aq.project.services.transaction.TransferTransactionService;
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

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static aq.project._utils.TransactionEntities.getValidTransferTransaction;
import static aq.project._utils.TransactionRequests.getInvalidTransferTransactionRequest;
import static aq.project._utils.TransactionRequests.getValidTransferTransactionRequest;
import static aq.project._utils.WalletEntities.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleTransactionRequestIntegrationTest {

    private static final String KAFKA_TEST_CONTAINER_TOPIC = "transfer_transaction_response";

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
    private TransferTransactionRepository transferTransactionRepository;

    @Autowired
    private TransferTransactionService transferTransactionService;

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
        transferTransactionRepository.deleteAll();
    }

    @Test
    public void successHandleTransactionRequest() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        Wallet senderWallet = getValidWallet(senderWalletId);
        Wallet recipientWallet = getValidWallet(recipientWalletId);
        TransferTransactionRequestDto request = getValidTransferTransactionRequest(transactionId, senderWalletId, recipientWalletId);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnDuplicateTransferTransaction() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        Wallet senderWallet = getValidWallet(senderWalletId);
        Wallet recipientWallet = getValidWallet(recipientWalletId);
        TransferTransactionRequestDto request = getValidTransferTransactionRequest(transactionId, senderWalletId, recipientWalletId);
        TransferTransaction transaction = getValidTransferTransaction(transactionId);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);
        transferTransactionRepository.save(transaction);

        // Assert
        Assertions.assertThrows(DuplicateTransactionHandleException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnSenderWalletNotFound() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        Wallet recipientWallet = getValidWallet(recipientWalletId);
        TransferTransactionRequestDto request = getValidTransferTransactionRequest(transactionId, senderWalletId, recipientWalletId);

        walletRepository.save(recipientWallet);

        // Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnRecipientWalletNotFound() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        Wallet senderWallet = getValidWallet(senderWalletId);
        TransferTransactionRequestDto request = getValidTransferTransactionRequest(transactionId, senderWalletId, recipientWalletId);

        walletRepository.save(senderWallet);

        // Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnSenderWalletBlocked() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        Wallet senderWallet = getValidWalletBlocked(senderWalletId);
        Wallet recipientWallet = getValidWallet(recipientWalletId);
        TransferTransactionRequestDto request = getValidTransferTransactionRequest(transactionId, senderWalletId, recipientWalletId);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnRecipientWalletBlocked() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        Wallet senderWallet = getValidWallet(senderWalletId);
        Wallet recipientWallet = getValidWalletBlocked(recipientWalletId);
        TransferTransactionRequestDto request = getValidTransferTransactionRequest(transactionId, senderWalletId, recipientWalletId);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnSenderCreditCardExpired() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        CreditCard expiredCard = getInvalidCreditCardExpired();

        Wallet senderWallet = getValidWallet(senderWalletId);
        senderWallet.setCreditCard(expiredCard);

        Wallet recipientWallet = getValidWallet(recipientWalletId);
        TransferTransactionRequestDto request = getValidTransferTransactionRequest(transactionId, senderWalletId, recipientWalletId);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnRecipientCreditCardExpired() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        CreditCard expiredCard = getInvalidCreditCardExpired();

        Wallet senderWallet = getValidWallet(senderWalletId);
        Wallet recipientWallet = getValidWallet(recipientWalletId);
        recipientWallet.setCreditCard(expiredCard);

        TransferTransactionRequestDto request = getValidTransferTransactionRequest(transactionId, senderWalletId, recipientWalletId);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnSenderInsufficientBalance() {
        // Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        Wallet senderWallet = getValidWallet(senderWalletId);
        // Устанавливаем баланс меньше суммы перевода (200.00)
        senderWallet.getCreditCard().setBalance(new java.math.BigDecimal("50.00"));

        Wallet recipientWallet = getValidWallet(recipientWalletId);
        TransferTransactionRequestDto request = getValidTransferTransactionRequest(transactionId, senderWalletId, recipientWalletId);

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnInvalidTransferTransactionRequest() {
        // Arrange & Act
        TransferTransactionRequestDto request = getInvalidTransferTransactionRequest();

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnNullTransferTransactionRequest() {
        // Arrange & Act
        TransferTransactionRequestDto request = null;

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transferTransactionService.handleTransactionRequest(request));
    }
}