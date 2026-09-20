package aq.project.test.controllers.withdraw_transaction_rest_controller;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
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

import java.util.UUID;

import static aq.project._utils.entities.transaction_service.WithdrawTransactionServiceEntities.getValidTransactionServiceWithdrawTransaction;
import static aq.project.controller.WithdrawTransactionRestControllerApi.PATH_GET_WITHDRAW_TRANSACTION_STATUS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWithdrawTransactionStatusWebTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    @Autowired
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void cleanRepositories() {
        transactionServiceWithdrawTransactionRepository.deleteAll();
        paymentProviderServiceTransactionRequestRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void successGetTransactionStatus() throws Exception {
//        Arrange
        TransactionServiceWithdrawTransaction transaction = getValidTransactionServiceWithdrawTransaction();
        transaction.setId(null);
        transaction.getTransactionMetadata().setId(null);
        transaction.getTransactionMetadata().setTimestamp(null);

        TransactionServiceWithdrawTransaction savedTransaction = transactionServiceWithdrawTransactionRepository.save(transaction);

        UUID transactionId = savedTransaction.getId();

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();
        String endpoint = PATH_GET_WITHDRAW_TRANSACTION_STATUS.replace("{id}", transactionId.toString());

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failGetTransactionStatusOnNotFoundTransaction() throws Exception {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();
        String endpoint = PATH_GET_WITHDRAW_TRANSACTION_STATUS.replace("{id}", transactionId.toString());

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failGetTransactionStatusOnLakeXTraceIdHeader() throws Exception {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String endpoint = PATH_GET_WITHDRAW_TRANSACTION_STATUS.replace("{id}", transactionId.toString());

//        Act & Assert
        mockMvc.perform(get(endpoint))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void failGetTransactionStatusOnUnauthorizedRequest() throws Exception {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();
        String endpoint = PATH_GET_WITHDRAW_TRANSACTION_STATUS.replace("{id}", transactionId.toString());

//        Act & Assert
        mockMvc.perform(get(endpoint)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isUnauthorized());
    }
}
