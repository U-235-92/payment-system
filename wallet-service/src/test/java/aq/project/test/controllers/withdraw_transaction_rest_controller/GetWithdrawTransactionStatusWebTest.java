package aq.project.test.controllers.withdraw_transaction_rest_controller;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.repositories.transaction.WithdrawTransactionRepository;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.security.SecureRandom;
import java.util.UUID;

import static aq.project._utils.TransactionEntities.getValidWithdrawTransaction;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWithdrawTransactionStatusWebTest {

    private static final String GET_TRANSACTION_STATUS_ENDPOINT = "/api/v1/transaction/withdraw/status/{id}";

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WithdrawTransactionRepository withdrawTransactionRepository;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void tearDownRepository() {
        withdrawTransactionRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void successGetTransactionStatus() throws Exception {
//        Arrange
        WithdrawTransaction transaction = getValidWithdrawTransaction();
        UUID transactionID = transaction.getId();

        withdrawTransactionRepository.save(transaction);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        String endpoint = GET_TRANSACTION_STATUS_ENDPOINT.replace("{id}", transactionID.toString());

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failGetTransactionStatusOnTransactionNotFound() throws Exception {
    //        Arrange
        WithdrawTransaction transaction = getValidWithdrawTransaction();
        UUID transactionID = UUID.randomUUID();

        withdrawTransactionRepository.save(transaction);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        String endpoint = GET_TRANSACTION_STATUS_ENDPOINT.replace("{id}", transactionID.toString());

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failGetWalletInfoOnLackTraceIdHeader() throws Exception {
//        Arrange
        WithdrawTransaction transaction = getValidWithdrawTransaction();
        UUID transactionID = transaction.getId();

        withdrawTransactionRepository.save(transaction);

        String endpoint = GET_TRANSACTION_STATUS_ENDPOINT.replace("{id}", transactionID.toString());

//        Act & Assert
        mockMvc.perform(get(endpoint))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void failCreateWalletOnUnauthorizedRequest() throws Exception {
//        Arrange
        WithdrawTransaction transaction = getValidWithdrawTransaction();
        UUID transactionID = transaction.getId();

        withdrawTransactionRepository.save(transaction);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = getTraceId();
        String endpoint = GET_TRANSACTION_STATUS_ENDPOINT.replace("{id}", transactionID.toString());

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue))
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
