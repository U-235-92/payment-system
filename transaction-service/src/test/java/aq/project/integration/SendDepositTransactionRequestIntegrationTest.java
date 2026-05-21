package aq.project.integration;

import aq.project.configs.ContainersConfigurer;
import aq.project.mocks.TestDepositTransactionRequestMocks;
import aq.project.services.TransactionService;
import aq.project.utils.Containers;
import jakarta.validation.ConstraintViolationException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SendDepositTransactionRequestIntegrationTest {

    private static AdminClient adminClient;

    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;

    @Autowired
    private TransactionService transactionService;

    @DynamicPropertySource
    static void configDynamicPropertySource(DynamicPropertyRegistry registry) {
        ContainersConfigurer.configureKafkaProperties(registry, KAFKA_CONTAINER);
    }

    @BeforeAll
    static void setup() {
        Properties adminClientProperties = new Properties();
        adminClientProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());

        NewTopic walletOperationRequestTopic = new NewTopic("wallet_operation_request", 3, (short) 1);

        adminClient = AdminClient.create(adminClientProperties);
        adminClient.createTopics(Collections.singleton(walletOperationRequestTopic));
    }

    @AfterAll
    static void teardown() {
        adminClient.close(Duration.ofSeconds(2L));
    }

    @Test
    public void successSendDepositTransactionRequestTest() {
        Assertions.assertDoesNotThrow(() -> transactionService
                .sendTransactionRequest(TestDepositTransactionRequestMocks.getValidDepositMessageRequest()));
    }

    @Test
    public void failSendDepositTransactionRequestWithWrongDataTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .sendTransactionRequest(TestDepositTransactionRequestMocks.getInvalidDepositMessageRequest()));
    }

    @Test
    public void failSendNullDepositTransactionRequestTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService
                .sendTransactionRequest(null));
    }
}
