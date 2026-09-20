package aq.project.test.handlers.ppstrh.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.utils.handlers.payment_provider_service.request.PaymentProviderServiceTransactionRequestHandler;
import dasniko.testcontainers.keycloak.KeycloakContainer;
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

import java.util.Collections;
import java.util.Properties;

import static aq.project._utils.entities.payment_provider_service.PaymentProviderServiceEntities.getValidPaymentProviderServiceTransactionRequest;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HandlePaymentProviderServiceSuccessResponseIntegrationTest {

    private static final String KAFKA_TEST_CONTAINER_TOPIC = "payment_provider_service_create_transaction_request";

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
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Autowired
    private PaymentProviderServiceTransactionRequestHandler paymentProviderServiceTransactionRequestHandler;

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

        NewTopic walletOperationRequestTopic = new NewTopic(KAFKA_TEST_CONTAINER_TOPIC, 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(Collections.singleton(walletOperationRequestTopic));
    }

    @AfterAll
    public static void cleanKafkaTopics() {
        adminClient.close();
    }

    @AfterEach
    void cleanRepositories() {
        transactionServiceDepositTransactionRepository.deleteAll();
        paymentProviderServiceTransactionRequestRepository.deleteAll();
    }

    @Test
    public void successHandlePaymentProviderServiceTransactionRequest() {
    //        Arrange
        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = getValidPaymentProviderServiceTransactionRequest();
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setId(null);
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);

        paymentProviderServiceTransactionRequestRepository.save(paymentProviderServiceTransactionRequest);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceTransactionRequestHandler.handlePaymentProviderServiceCreateTransactionRequest(paymentProviderServiceTransactionRequest));
        Assertions.assertTrue(paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().isProcessed());
    }
}
