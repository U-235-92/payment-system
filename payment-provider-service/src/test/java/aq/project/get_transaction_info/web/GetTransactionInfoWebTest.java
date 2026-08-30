package aq.project.get_transaction_info.web;

import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project._utils.Containers;
import aq.project._utils.ContainerPropertiesConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
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

import static aq.project.controller.TransactionRestControllerApi.PATH_GET_TRANSACTION_INFO;
import static aq.project._utils.Entities.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionInfoWebTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void tearDownRepositories() {
        merchantRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "USER")
    public void successGetTransactionInfoWebTest() throws Exception {
//        Arrange
        Transaction transaction = getValidPendingTransaction();
        Merchant merchant = getValidMerchant();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        mockMvc.perform(get(PATH_GET_TRANSACTION_INFO.replace("{id}", transaction.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "USER")
    public void failGetTransactionInfoOnInvalidParametersWebTest() throws Exception {
//        Arrange
        String transactionId = getInvalidTransactionId();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(get(PATH_GET_TRANSACTION_INFO.replace("{id}", transactionId))
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "ADMIN")
    public void failGetTransactionInfoOnUnexpectedRoleWebTest() throws Exception {
//        Arrange
        String transactionId = getValidTransactionId();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(get(PATH_GET_TRANSACTION_INFO.replace("{id}", transactionId))
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue))
                .andExpect(status().isForbidden());
    }

    @Test
    public void failGetTransactionInfoOnUnauthorizedRequestWebTest() throws Exception {
//        Arrange
        String transactionId = getValidTransactionId();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(get(PATH_GET_TRANSACTION_INFO.replace("{id}", transactionId))
                        .header(xTraceKey, xTraceValue))
                .andExpect(status().isUnauthorized());
    }
}
