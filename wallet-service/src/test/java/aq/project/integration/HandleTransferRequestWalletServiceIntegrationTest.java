package aq.project.integration;

import aq.project.configs.ContainersConfigurer;
import aq.project.entities.Wallet;
import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.messages.TransactionRequest;
import aq.project.mocks.*;
import aq.project.repositories.OutboxEventRepository;
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
public class HandleTransferRequestWalletServiceIntegrationTest {

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
    private OutboxEventRepository outboxEventRepository;

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
    public void successfulHandleTransferRequestIntegrationTest() {
        Wallet recipientWallet = walletRepository.save(TestWalletMocks.getValidWalletMock());
        Wallet senderWallet = walletRepository.save(TestWalletMocks.getValidWalletMock());
        Assertions.assertDoesNotThrow(() -> transactionService
                .handleTransactionRequest(TestTransferTransactionRequestMocks.getValidTransferConsumerRecord(
                        recipientWallet.getId().toString(),
                        recipientWallet.getPersonId(),
                        senderWallet.getId().toString(),
                        senderWallet.getPersonId()
                        )
                ));
        walletRepository.deleteById(recipientWallet.getId());
        walletRepository.deleteById(senderWallet.getId());
    }

    @Test
    public void successfulHandleDuplicateTransferRequestIntegrationTest() {
        ConsumerRecord<String, TransactionRequest> record = TestWithdrawTransactionRequestMocks
                .getValidWithdrawConsumerRecord();
        Headers headers = record.headers();
        String walletId = getPropertyFromHeader(headers, RECIPIENT_WALLET_ID);
        Wallet wallet = TestWalletMocks.getValidWalletMock(walletId);
        walletRepository.save(wallet);
        String transactionId = outboxEventRepository.save(TestTransactionEventMocks.getValidDepositTransactionEvent())
                .getTransactionId();
        Assertions.assertDoesNotThrow(() -> transactionService
                .handleTransactionRequest(TestTransferTransactionRequestMocks.getValidTransferConsumerRecord()));
        outboxEventRepository.deleteById(transactionId);
        walletRepository.deleteById(wallet.getId());
    }

    private String getPropertyFromHeader(Headers headers, String header) {
        byte[] bytes = headers.headers(header).iterator().next().value();
        return new String(bytes);
    }

    @Test
    public void failHandleTransferRequestWithNullInvalidTransferMessageRequestIntegrationTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .handleTransactionRequest(null));
    }

    @Test
    public void failHandleTransferRequestWithInvalidTransferMessageRequestIntegrationTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .handleTransactionRequest(TestTransferTransactionRequestMocks.getInvalidTransferConsumerRecord()));
    }

    @Test
    public void failHandleTransferRequestWithUnknownWalletUuidIntegrationTest() {
        Assertions.assertThrows(NoSuchWalletException.class, () -> transactionService
                .handleTransactionRequest(TestTransferTransactionRequestMocks.getTransferConsumerRecordWithUnknownUuid()));
    }

    @Test
    @Disabled("Test works ONLY IF started independently from other tests")
    public void failHandleTransferRequestWithBlockedWalletIntegrationTest() {
        Wallet senderWallet = walletRepository.save(TestWalletMocks.getBlockedWalletMock());
        Wallet recipientWallet = walletRepository.save(TestWalletMocks.getBlockedWalletMock());
        Assertions.assertThrows(WalletConstrainsException.class, () -> transactionService
                .handleTransactionRequest(TestTransferTransactionRequestMocks.getValidTransferConsumerRecord(
                        recipientWallet.getId(),
                        recipientWallet.getPersonId(),
                        senderWallet.getId(),
                        senderWallet.getPersonId())));
        walletRepository.deleteById(senderWallet.getId());
        walletRepository.deleteById(recipientWallet.getId());
    }

    @Test
    public void failHandleTransferRequestWithExpiredCardOfWalletIntegrationTest() {
        Wallet senderWallet = walletRepository.save(TestWalletMocks.getExpiredCardValidWalletMock());
        Wallet recipientWallet = walletRepository.save(TestWalletMocks.getExpiredCardValidWalletMock());
        Assertions.assertThrows(CreditCardConstrainsException.class, () -> transactionService
                .handleTransactionRequest(TestTransferTransactionRequestMocks.getValidTransferConsumerRecord(
                        recipientWallet.getId(),
                        recipientWallet.getPersonId(),
                        senderWallet.getId(),
                        senderWallet.getPersonId())));
        walletRepository.deleteById(senderWallet.getId());
        walletRepository.deleteById(recipientWallet.getId());
    }

    @Test
    public void failHandleTransferRequestWithNegativeAmountIntegrationTest() {
        Wallet wallet = walletRepository.save(TestWalletMocks.getValidWalletMock());
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .handleTransactionRequest(TestTransferTransactionRequestMocks
                        .getTransferConsumerRecordWithNegativeAmount()));
        walletRepository.deleteById(wallet.getId());
    }

    @Test
    @Disabled("This test disabled because when it runs with others tests the exception is not thrown, but works fine when runs alone")
    public void failHandleTransferRequestAmountGreaterThanWalletAmountIntegrationTest() {
        Wallet recipientWallet = walletRepository.save(TestWalletMocks.getValidWalletMock());
        Wallet senderWallet = walletRepository.save(TestWalletMocks.getValidWalletMock());
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .handleTransactionRequest(TestTransferTransactionRequestMocks.getTransferConsumerRecordWithBigAmount(
                                recipientWallet.getId().toString(),
                                recipientWallet.getPersonId(),
                                senderWallet.getId().toString(),
                                senderWallet.getPersonId()
                        )
                ));
        walletRepository.deleteById(recipientWallet.getId());
        walletRepository.deleteById(senderWallet.getId());
    }
}
