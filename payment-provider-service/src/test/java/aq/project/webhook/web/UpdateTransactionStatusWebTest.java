package aq.project.webhook.web;

import aq.project.dto.TransactionStatusDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.repositories.WebhookRepository;
import aq.project._utils.Containers;
import aq.project._utils.ContainerPropertiesConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
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

import java.util.UUID;

import static aq.project.controller.WebhookRestControllerApi.PATH_UPDATE_TRANSACTION_STATUS;
import static aq.project._utils.Entities.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateTransactionStatusWebTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WebhookRepository webhookRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void tearDownDb() {
        transactionRepository.deleteAll();
        webhookRepository.deleteAll();
        merchantRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "USER")
    public void successUpdateTransactionStatusWebTest() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();
        Transaction transaction = getValidTransaction();
        TransactionStatusDto transactionStatusDto = getValidTransactionStatusDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        mockMvc.perform(post(PATH_UPDATE_TRANSACTION_STATUS)
                .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                .header(xTraceKey, xTraceValue)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(transactionStatusDto)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "USER")
    public void failUpdateTransactionStatusOnInvalidTransactionStatusDtoWebTest() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();
        Transaction transaction = getValidTransaction();
        TransactionStatusDto transactionStatusDto = getInvalidTransactionStatusDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        mockMvc.perform(post(PATH_UPDATE_TRANSACTION_STATUS)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(transactionStatusDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "ADMIN")
    public void failUpdateTransactionStatusOnUnexpectedRoleWebTest() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();
        Transaction transaction = getValidTransaction();
        TransactionStatusDto transactionStatusDto = getValidTransactionStatusDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        mockMvc.perform(post(PATH_UPDATE_TRANSACTION_STATUS)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(transactionStatusDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void failUpdateTransactionStatusOnUnauthorizedRequestWebTest() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();
        Transaction transaction = getValidTransaction();
        TransactionStatusDto transactionStatusDto = getValidTransactionStatusDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

//        Act & Assert
        mockMvc.perform(post(PATH_UPDATE_TRANSACTION_STATUS)
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(transactionStatusDto)))
                .andExpect(status().isUnauthorized());
    }
}
