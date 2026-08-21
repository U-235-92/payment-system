package aq.project.message_broker_handler.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.Transaction;
import aq.project.utils.handlers.MessageBrokerHandler;
import aq.project.utils.telemetry.TraceContext;
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

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static aq.project._utils.Entities.getInvalidPendingNotProcessedTransaction;
import static aq.project._utils.Entities.getValidPendingNotProcessedTransaction;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SendTransactionRequestToMessageBrokerIntegrationTest {

    private static final String WALLET_OPERATION_REQUEST_TOPIC_NAME = "wallet_operation_request";

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
    private TraceContext traceContext;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKafkaContainerProperties(registry, KAFKA_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @BeforeAll
    static void setupKafka() {
        Properties adminClientProperties = new Properties();
        adminClientProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());

        NewTopic walletOperationRequestTopic = new NewTopic(WALLET_OPERATION_REQUEST_TOPIC_NAME, 3, (short) 1);

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
    public void successSendTransactionRequestToMessageBrokerIntegrationTest() {
//        Arrange
        Transaction transaction = getValidPendingNotProcessedTransaction();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> messageBrokerHandler.sendTransactionRequestToMessageBroker(transaction));
    }

    @Test
    public void failSendTransactionRequestToMessageBrokerOnInvalidTransactionIntegrationTest() {
//        Arrange
        Transaction transaction = getInvalidPendingNotProcessedTransaction();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> messageBrokerHandler.sendTransactionRequestToMessageBroker(transaction));
    }
}
