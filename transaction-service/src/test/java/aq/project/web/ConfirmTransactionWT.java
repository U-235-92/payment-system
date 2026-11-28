package aq.project.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.ConfirmTransactionRequestDto;
import aq.project.dto.PaymentType;
import aq.project.services.TokenService;
import aq.project.utils.jwt.JwtUtil;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static aq.project.controller.TransactionRestControllerApi.PATH_CONFIRM_TRANSACTION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(value = @ConfigureWireMock(name = "wallet-service", port = 18085))
public class ConfirmTransactionWT {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static AdminClient adminClient;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);

        ContainerPropertiesConfigurer
                .registerApplicationContextKafkaContainerProperties(registry, KAFKA_CONTAINER);

        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @BeforeAll
    public static void createKafkaTopics() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());

        NewTopic depositTransactionRequestTopic = new NewTopic("deposit_transaction_request", 3, (short) 1);

        adminClient = AdminClient.create(props);
        adminClient.createTopics(Collections.singleton(depositTransactionRequestTopic));
    }

    @AfterAll
    public static void cleanKafkaTopics() {
        adminClient.close();
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void successConfirmTransactionWithMockUser() throws Exception {
//        Arrange
        UUID walletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(null);
        confirmTransactionRequestDto.setTargetWalletConversionRate(null);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.DEPOSIT);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String json = objectMapper.writeValueAsString(confirmTransactionRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_CONFIRM_TRANSACTION)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void successConfirmTransactionWithAuthorization() throws Exception {
//        Arrange
        UUID walletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(null);
        confirmTransactionRequestDto.setTargetWalletConversionRate(null);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.DEPOSIT);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());

        String json = objectMapper.writeValueAsString(confirmTransactionRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_CONFIRM_TRANSACTION)
                        .header("Authorization", authorization)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void failureConfirmTransactionOnUnauthorizedRequest() throws Exception {
//        Arrange
        UUID walletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(null);
        confirmTransactionRequestDto.setTargetWalletConversionRate(null);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.DEPOSIT);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String json = objectMapper.writeValueAsString(confirmTransactionRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_CONFIRM_TRANSACTION)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}