package aq.project.transaction_service.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionRequestDto;
import aq.project.utils.telemetry.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.*;
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
import org.wiremock.spring.InjectWireMock;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static aq.project._utils.Entities.getInvalidTransactionRequestDto;
import static aq.project._utils.Entities.getValidTransactionRequestDto;
import static aq.project.controller.TransactionRestControllerApi.PATH_SEND_TRANSACTION_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "payment-provider-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SendTransactionRequestWebTest {

    private static final String WALLET_OPERATION_REQUEST_TOPIC_NAME = "wallet_operation_request";

    private static final String PAYMENT_SERVICE_CREATE_TRANSACTION_ENDPOINT = "/api/v1/transactions/create";

    private static AdminClient adminClient;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;
    @Container
    private static final KafkaContainer KAFKA_CONTAINER = Containers.KAFKA_CONTAINER;
    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private MockMvc mockMvc;

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
    @WithMockUser(username = "user", password = "secret")
    public void successSendTransactionRequestWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        TransactionRequestDto dto = getValidTransactionRequestDto();

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_CREATE_TRANSACTION_ENDPOINT)
                .willReturn(WireMock.created()));

//        Act & Assert
        mockMvc.perform(post(PATH_SEND_TRANSACTION_REQUEST)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failSendTransactionRequestOn4xxPaymentServiceResponseWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        TransactionRequestDto dto = getValidTransactionRequestDto();

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_CREATE_TRANSACTION_ENDPOINT)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        mockMvc.perform(post(PATH_SEND_TRANSACTION_REQUEST)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failSendTransactionRequestOn5xxPaymentServiceResponseWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        TransactionRequestDto dto = getValidTransactionRequestDto();

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_CREATE_TRANSACTION_ENDPOINT)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        mockMvc.perform(post(PATH_SEND_TRANSACTION_REQUEST)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failSendTransactionRequestOnInvalidTransactionRequestDtoWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        TransactionRequestDto dto = getInvalidTransactionRequestDto();

//        Act & Assert
        mockMvc.perform(post(PATH_SEND_TRANSACTION_REQUEST)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void failSendTransactionRequestOnUnauthorizedRequestWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        TransactionRequestDto dto = getValidTransactionRequestDto();

//        Act & Assert
        mockMvc.perform(post(PATH_SEND_TRANSACTION_REQUEST)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    private String getTraceId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder xTraceId = new StringBuilder();
        for(byte b : bytes) {
            xTraceId.append(String.format("%02x", b));
        }
        return xTraceId.toString();
    }
}
