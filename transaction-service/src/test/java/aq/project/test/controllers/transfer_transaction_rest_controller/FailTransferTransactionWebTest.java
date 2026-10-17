package aq.project.test.controllers.transfer_transaction_rest_controller;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceTransferTransactionRequestRepository;
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

import static aq.project._utils.entities.transaction_service.TransferTransactionServiceEntities.getValidTransactionServiceTransferTransaction;
import static aq.project.controller.TransferTransactionRestControllerApi.PATH_FAIL_TRANSFER_TRANSACTION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FailTransferTransactionWebTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private TransactionServiceTransferTransactionRepository transactionServiceTransferTransactionRepository;
    @Autowired
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;
    @Autowired
    private WalletServiceTransferTransactionRequestRepository walletServiceTransferTransactionRequestRepository;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void cleanRepositories() {
        transactionServiceTransferTransactionRepository.deleteAll();
        paymentProviderServiceTransactionRequestRepository.deleteAll();
        walletServiceTransferTransactionRequestRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void successFailTransaction() throws Exception {
//        Arrange
        TransactionServiceTransferTransaction newTransactionServiceTransferTransaction = getValidTransactionServiceTransferTransaction();
        newTransactionServiceTransferTransaction.setId(null);
        newTransactionServiceTransferTransaction.setStatus(TransactionStatus.PENDING);
        newTransactionServiceTransferTransaction.getTransactionMetadata().setId(null);

        TransactionServiceTransferTransaction savedTransactionServiceTransferTransaction = transactionServiceTransferTransactionRepository
                .save(newTransactionServiceTransferTransaction);

        UUID transactionId = savedTransactionServiceTransferTransaction.getId();

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String uri = PATH_FAIL_TRANSFER_TRANSACTION.replace("{id}", transactionId.toString());

//        Act & Assert
        mockMvc.perform(post(uri)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void failFailTransactionOnLakeTransactionId() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String uri = PATH_FAIL_TRANSFER_TRANSACTION.replace("{id}", "");

//        Act & Assert
        mockMvc.perform(post(uri)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void failFailTransactionOnUnauthorizedRequest() throws Exception {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String uri = PATH_FAIL_TRANSFER_TRANSACTION.replace("{id}", transactionId.toString());

//        Act & Assert
        mockMvc.perform(post(uri)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void failFailTransactionOnLakeXTraceIdHeader() throws Exception {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String uri = PATH_FAIL_TRANSFER_TRANSACTION.replace("{id}", transactionId.toString());

//        Act & Assert
        mockMvc.perform(post(uri))
                .andExpect(status().isBadRequest());
    }
}
