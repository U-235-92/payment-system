package aq.project.integration;

import aq.project.configs.ContainersConfigurer;
import aq.project.entities.Wallet;
import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.messages.TransactionRequest;
import aq.project.mocks.TestTransactionEventMocks;
import aq.project.mocks.TestWalletMocks;
import aq.project.mocks.TestWithdrawTransactionRequestMocks;
import aq.project.repositories.TransactionRepository;
import aq.project.repositories.WalletRepository;
import aq.project.services.TransactionService;
import aq.project.utils.Containers;
import jakarta.validation.ConstraintViolationException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
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

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static aq.project.util.RequestPropertyKeys.RECIPIENT_WALLET_ID;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandleWithdrawRequestWalletServiceIntegrationTest {

    private static AdminClient adminClient;

    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @DynamicPropertySource
    static void configDynamicPropertySource(DynamicPropertyRegistry registry) {
        ContainersConfigurer.configureKafkaProperties(registry, KAFKA_CONTAINER);
        ContainersConfigurer.configurePostgreSqlProperties(registry, POSTGRESQL_CONTAINER);
    }

    @BeforeAll
    static void setup() {
        Properties adminClientProperties = new Properties();
        adminClientProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());

        NewTopic walletOperationRequestTopic = new NewTopic("wallet_operation_request", 3, (short) 1);
        NewTopic walletOperationResponseTopic = new NewTopic("wallet_operation_response", 3, (short) 1);

        adminClient = AdminClient.create(adminClientProperties);
        adminClient.createTopics(Collections.singleton(walletOperationRequestTopic));
        adminClient.createTopics(Collections.singleton(walletOperationResponseTopic));
    }

    @AfterAll
    static void teardown() {
        adminClient.close(Duration.ofSeconds(2L));
    }

    @Test
    public void successfulHandleWithdrawRequestIntegrationTest() {
        Wallet wallet = walletRepository.save(TestWalletMocks.getValidWalletMock());
        Assertions.assertDoesNotThrow(() -> transactionService
    .handleTransactionRequest(TestWithdrawTransactionRequestMocks
            .getValidWithdrawConsumerRecord(wallet.getId().toString(), wallet.getPersonId())));
        walletRepository.deleteById(wallet.getId());
    }

    @Test
    public void successfulHandleDuplicateWithdrawRequestIntegrationTest() {
        ConsumerRecord<String, TransactionRequest> record = TestWithdrawTransactionRequestMocks
                .getValidWithdrawConsumerRecord();
        Headers headers = record.headers();
        String walletId = getPropertyFromHeader(headers, RECIPIENT_WALLET_ID);
        Wallet wallet = TestWalletMocks.getValidWalletMock(walletId);
        walletRepository.save(wallet);
        String transactionId = transactionRepository.save(TestTransactionEventMocks
                .getValidDepositTransactionEvent()).getTransactionId();
        Assertions.assertDoesNotThrow(() -> transactionService
                .handleTransactionRequest(TestWithdrawTransactionRequestMocks
                        .getValidWithdrawConsumerRecord()));
        transactionRepository.deleteById(transactionId);
        walletRepository.deleteById(wallet.getId());
    }

    private String getPropertyFromHeader(Headers headers, String header) {
        byte[] bytes = headers.headers(header).iterator().next().value();
        return new String(bytes);
    }

    @Test
    public void failHandleWithdrawRequestWithNullInvalidWithdrawMessageRequestIntegrationTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .handleTransactionRequest(null));
    }

    @Test
    public void failHandleWithdrawRequestWithInvalidWithdrawMessageRequestIntegrationTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .handleTransactionRequest(TestWithdrawTransactionRequestMocks
                        .getInvalidWithdrawConsumerRecord()));
    }

    @Test
    public void failHandleWithdrawRequestWithUnknownWalletUuidIntegrationTest() {
        Assertions.assertThrows(NoSuchWalletException.class, () -> transactionService
                .handleTransactionRequest(TestWithdrawTransactionRequestMocks
                        .getWithdrawConsumerRecordWithUnknownUuid()));
    }

    @Test
    @Disabled("Test works ONLY IF started independently from other tests")
    public void failHandleWithdrawRequestWithBlockedWalletIntegrationTest() {
        Wallet wallet = walletRepository.save(TestWalletMocks.getBlockedWalletMock());
        Assertions.assertThrows(WalletConstrainsException.class, () -> transactionService
                .handleTransactionRequest(TestWithdrawTransactionRequestMocks
                        .getValidWithdrawConsumerRecord(wallet.getId(), wallet.getPersonId())));
        walletRepository.deleteById(wallet.getId());
    }

    @Test
    @Disabled("Test works ONLY IF started independently from other tests")
    public void failHandleWithdrawRequestWithExpiredCardOfWalletIntegrationTest() {
        Wallet wallet = walletRepository.save(TestWalletMocks.getExpiredCardValidWalletMock());
        Assertions.assertThrows(CreditCardConstrainsException.class, () -> transactionService
                .handleTransactionRequest(TestWithdrawTransactionRequestMocks
                        .getValidWithdrawConsumerRecord(wallet.getId(), wallet.getPersonId())));
        walletRepository.deleteById(wallet.getId());
    }

    @Test
    public void failHandleWithdrawRequestWithNegativeAmountIntegrationTest() {
        Wallet wallet = walletRepository.save(TestWalletMocks.getValidWalletMock());
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .handleTransactionRequest(TestWithdrawTransactionRequestMocks
                        .getWithdrawConsumerRecordWithNegativeAmount()));
        walletRepository.deleteById(wallet.getId());
    }

    @Test
    public void failHandleWithdrawRequestWithAmountGreaterThanWalletAmountIntegrationTest() {
        Wallet wallet = walletRepository.save(TestWalletMocks.getValidWalletMock());
        Assertions.assertThrows(CreditCardConstrainsException.class, () -> transactionService
                .handleTransactionRequest(TestWithdrawTransactionRequestMocks
                        .getWithdrawConsumerRecordWithBigAmount(wallet.getId().toString(), wallet.getPersonId())));
        walletRepository.deleteById(wallet.getId());
    }
}
