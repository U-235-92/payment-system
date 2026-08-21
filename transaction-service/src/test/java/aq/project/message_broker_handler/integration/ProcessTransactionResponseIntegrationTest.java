package aq.project.message_broker_handler.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.Transaction;
import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.ServiceHttpException;
import aq.project.messages.TransactionResponse;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.MessageBrokerHandler;
import aq.project.utils.telemetry.TraceContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static aq.project._utils.Entities.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "payment-provider-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProcessTransactionResponseIntegrationTest {

    private static final String WALLET_OPERATION_RESPONSE_TOPIC_NAME = "wallet_operation_response";
    private static final String PAYMENT_SERVICE_WEBHOOK_ENDPOINT = "/api/v1/webhook/transactions/update-status";
    private static final String PAYMENT_SERVICE_CANCEL_TRANSACTION_ENDPOINT = "/api/v1/transactions/cancel";

    private static AdminClient adminClient;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;
    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;
    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private MessageBrokerHandler messageBrokerHandler;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TraceContext traceContext;

    @InjectWireMock("payment-provider-service-mock")
    private WireMockServer paymentProviderServiceMockServer;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKafkaContainerProperties(registry, KAFKA_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        registry.add("service.payment-provider-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @BeforeAll
    static void setupKafka() {
        Properties adminClientProperties = new Properties();
        adminClientProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());

        NewTopic walletOperationRequestTopic = new NewTopic(WALLET_OPERATION_RESPONSE_TOPIC_NAME, 3, (short) 1);

        adminClient = AdminClient.create(adminClientProperties);
        adminClient.createTopics(Collections.singleton(walletOperationRequestTopic));
    }

    @AfterAll
    static void teardownKafka() {
        Duration duration = Duration.ofSeconds(2L);
        adminClient.close(duration);
    }

    @BeforeEach
    public void propagateTraceId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder xTraceId = new StringBuilder();
        for(byte b : bytes) {
            xTraceId.append(String.format("%02x", b));
        }
        traceContext.setTraceId(xTraceId.toString());
    }

    @AfterEach
    public void cleanTraceContext() {
        traceContext.clean();
    }

    @Test
    public void successProcessTransactionResponseIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Transaction transaction = getValidPendingNotProcessedTransaction(transactionId);
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidCompletedConsumerRecord(transactionId);

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_WEBHOOK_ENDPOINT)
                .willReturn(WireMock.ok()));

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> messageBrokerHandler.processTransactionResponse(consumerRecord));
    }

    @Test
    public void failProcessTransactionResponseOnPaymentService5xxErrorResponseIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Transaction transaction = getValidPendingNotProcessedTransaction(transactionId);
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidCompletedConsumerRecord(transactionId);

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_WEBHOOK_ENDPOINT)
                .willReturn(WireMock.serverError()));

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(ServiceHttpException.class,
                () -> messageBrokerHandler.processTransactionResponse(consumerRecord));
    }

    @Test
    public void failProcessTransactionResponseOnPaymentService4xxErrorResponseIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Transaction transaction = getValidPendingNotProcessedTransaction(transactionId);
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidCompletedConsumerRecord(transactionId);

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_WEBHOOK_ENDPOINT)
                .willReturn(WireMock.badRequest()));

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(ClientHttpException.class,
                () -> messageBrokerHandler.processTransactionResponse(consumerRecord));
    }

    @Test
    public void successProcessTransactionResponseOnCancelTransactionRequestIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Transaction transaction = getValidPendingNotProcessedTransaction(transactionId);
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidFailedConsumerRecord(transactionId);

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_CANCEL_TRANSACTION_ENDPOINT)
                .willReturn(WireMock.ok()));

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> messageBrokerHandler.processTransactionResponse(consumerRecord));
    }

    @Test
    public void failProcessTransactionResponseOnCancelTransactionRequest4xxResponseIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Transaction transaction = getValidPendingNotProcessedTransaction(transactionId);
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidFailedConsumerRecord(transactionId);

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_CANCEL_TRANSACTION_ENDPOINT)
                .willReturn(WireMock.badRequest()));

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(ClientHttpException.class,
                () -> messageBrokerHandler.processTransactionResponse(consumerRecord));
    }

    @Test
    public void failProcessTransactionResponseOnCancelTransactionRequest5xxResponseIntegrationTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Transaction transaction = getValidPendingNotProcessedTransaction(transactionId);
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidFailedConsumerRecord(transactionId);

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_CANCEL_TRANSACTION_ENDPOINT)
                .willReturn(WireMock.serverError()));

        transactionRepository.save(transaction);

//        Act & Assert
        Assertions.assertThrows(ServiceHttpException.class,
                () -> messageBrokerHandler.processTransactionResponse(consumerRecord));
    }

    @Test
    public void failProcessTransactionResponseOnNullConsumerRecordResponseIntegrationTest() {
//        Arrange
        ConsumerRecord<String, TransactionResponse> consumerRecord = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> messageBrokerHandler.processTransactionResponse(consumerRecord));
    }

    @Test
    public void failProcessTransactionResponseOnInvalidConsumerRecordResponseValueIntegrationTest() {
//        Arrange
        ConsumerRecord<String, TransactionResponse> consumerRecord = getInvalidConsumerRecord();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> messageBrokerHandler.processTransactionResponse(consumerRecord));
    }
}
