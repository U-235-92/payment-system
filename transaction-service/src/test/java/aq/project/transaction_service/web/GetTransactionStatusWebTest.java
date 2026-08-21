package aq.project.transaction_service.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import dasniko.testcontainers.keycloak.KeycloakContainer;
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
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.security.SecureRandom;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "wallet-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionStatusWebTest {

    private static final String WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT = "/api/v1/transaction/status";

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;
    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private MockMvc mockMvc;

    @InjectWireMock("wallet-service-mock")
    private WireMockServer walletServiceMockServer;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        registry.add("service.wallet-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void successGetTransactionStatusWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        String transactionId = UUID.randomUUID().toString();
        String endpoint = String.format("%s/%s", WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT, transactionId);
        String contentTypeHeaderName = "Content-Type";
        String contentTypeHeaderValue = "application/json";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(TransactionStatus.PENDING);

        ResponseDefinition responseDefinition = ResponseDefinitionBuilder.responseDefinition()
                .withStatus(200)
                .withHeader(contentTypeHeaderName, contentTypeHeaderValue)
                .withJsonBody(jsonNode)
                .build();

        walletServiceMockServer.stubFor(WireMock.get(endpoint)
                .willReturn(ResponseDefinitionBuilder.like(responseDefinition)));

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failGetTransactionStatusOn4xxWalletServiceResponseWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        String transactionId = UUID.randomUUID().toString();
        String endpoint = String.format("%s/%s", WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT, transactionId);
        String contentTypeHeaderName = "Content-Type";
        String contentTypeHeaderValue = "application/json";

        ResponseDefinition responseDefinition = ResponseDefinitionBuilder.responseDefinition()
                .withStatus(400)
                .withHeader(contentTypeHeaderName, contentTypeHeaderValue)
                .build();

        walletServiceMockServer.stubFor(WireMock.get(endpoint)
                .willReturn(ResponseDefinitionBuilder.like(responseDefinition)));

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failGetTransactionStatusOn5xxWalletServiceResponseWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        String transactionId = UUID.randomUUID().toString();
        String endpoint = String.format("%s/%s", WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT, transactionId);
        String contentTypeHeaderName = "Content-Type";
        String contentTypeHeaderValue = "application/json";

        ResponseDefinition responseDefinition = ResponseDefinitionBuilder.responseDefinition()
                .withStatus(500)
                .withHeader(contentTypeHeaderName, contentTypeHeaderValue)
                .build();

        walletServiceMockServer.stubFor(WireMock.get(endpoint)
                .willReturn(ResponseDefinitionBuilder.like(responseDefinition)));

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failGetTransactionStatusOnInvalidTransactionIdWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        String transactionId = "invalid-transaction-id";
        String endpoint = String.format("%s/%s", WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT, transactionId);

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void failGetTransactionStatusOnUnauthorizedRequestWebTest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        String transactionId = UUID.randomUUID().toString();
        String endpoint = String.format("%s/%s", WALLET_SERVICE_GET_TRANSACTION_STATUS_ENDPOINT, transactionId);
        String contentTypeHeaderName = "Content-Type";
        String contentTypeHeaderValue = "application/json";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(TransactionStatus.PENDING);

        ResponseDefinition responseDefinition = ResponseDefinitionBuilder.responseDefinition()
                .withStatus(200)
                .withHeader(contentTypeHeaderName, contentTypeHeaderValue)
                .withJsonBody(jsonNode)
                .build();

        walletServiceMockServer.stubFor(WireMock.get(endpoint)
                .willReturn(ResponseDefinitionBuilder.like(responseDefinition)));

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON))
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
