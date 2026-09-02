package aq.project.test.services.deposit_transaction_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.dto.DepositTransactionRequestDto;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.repositories.wallet.WalletRepository;
import aq.project.services.transaction.DepositTransactionService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
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

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static aq.project._utils.WalletEntities.*;
import static aq.project._utils.TransactionEntities.*;
import static aq.project._utils.TransactionRequests.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleTransactionRequestIntegrationTest {

    private static final String KAFKA_TEST_CONTAINER_TOPIC = "deposit_transaction_response";

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
        depositTransactionRepository.deleteAll();
    }

    @Test
    public void successHandleTransactionRequest() {
//        Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWallet(walletId);
        DepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

//        Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnDuplicateDepositTransaction() {
//        Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWallet(walletId);
        DepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);
        DepositTransaction transaction = getValidDepositTransaction(transactionId);

        walletRepository.save(wallet);
        depositTransactionRepository.save(transaction);

//        Assert
        Assertions.assertThrows(DuplicateTransactionHandleException.class,
                () -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnWalletNotFound() {
//        Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        DepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);

//        Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnWalletBlocked() {
//        Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = getValidWalletBlocked(walletId);
        DepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

//        Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnWalletCreditCardExpired() {
//        Arrange & Act
        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        CreditCard creditCard = getInvalidCreditCardExpired();

        Wallet wallet = getValidWallet(walletId);
        wallet.setCreditCard(creditCard);

        DepositTransactionRequestDto request = getValidDepositTransactionRequest(transactionId, walletId);

        walletRepository.save(wallet);

//        Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnInvalidDepositTransactionRequest() {
//        Arrange & Act
        DepositTransactionRequestDto request = getInvalidDepositTransactionRequest();

//        Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.handleTransactionRequest(request));
    }

    @Test
    public void failHandleTransactionRequestOnNullDepositTransactionRequest() {
//        Arrange & Act
        DepositTransactionRequestDto request = null;

//        Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.handleTransactionRequest(request));
    }
}
