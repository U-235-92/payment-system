package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.ConfirmTransactionRequestDto;
import aq.project.dto.PaymentType;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.TransactionService;
import com.github.tomakehurst.wiremock.WireMockServer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
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
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(value = @ConfigureWireMock(name = "wallet-service", port = 18085))
public class ConfirmWithdrawalTransactionWithDtoParameterIT {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionService transactionService;

    private static AdminClient adminClient;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);

        ContainerPropertiesConfigurer
                .registerApplicationContextKafkaContainerProperties(registry, KAFKA_CONTAINER);

        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);

        registry.add("app.service.wallet-service.uri", () -> "http://localhost:18085");
    }

    @BeforeAll
    public static void createKafkaTopics() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());

        NewTopic withdrawalTransactionRequestTopic = new NewTopic("withdrawal_transaction_request", 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(Collections.singleton(withdrawalTransactionRequestTopic));
    }

    @AfterAll
    public static void cleanKafkaTopics() {
        adminClient.close();
    }

    @Test
    public void successConfirmTransaction() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(null);
        confirmTransactionRequestDto.setTargetWalletConversionRate(null);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.confirmTransaction(confirmTransactionRequestDto));
    }

    @Test
    public void failureOnInvalidRequestDto() {
//        Arrange
        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(null);
        confirmTransactionRequestDto.setTargetWalletId(null);
        confirmTransactionRequestDto.setTargetWalletConversionRate(null);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(-50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.confirmTransaction(confirmTransactionRequestDto));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(null);
        confirmTransactionRequestDto.setTargetWalletConversionRate(null);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> transactionService.confirmTransaction(confirmTransactionRequestDto));
    }
}