package aq.project.transaction.web;

import aq.project.dto.TransactionRequestDto;
import aq.project.entities.Merchant;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
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

import static aq.project.controller.TransactionRestControllerApi.PATH_CREATE_TRANSACTION;
import static aq.project._utils.Entities.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateTransactionWebTest {

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
    public void successCreateTransactionWebTest() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();
        TransactionRequestDto requestDto = getValidTransactionRequestDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_TRANSACTION)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "USER")
    public void failCreateTransactionOnInvalidRequestDtoWebTest() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();
        TransactionRequestDto requestDto = getInvalidTransactionRequestDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_TRANSACTION)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "ADMIN")
    public void failCreateTransactionOnUnexpectedRoleWebTest() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();
        TransactionRequestDto requestDto = getValidTransactionRequestDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_TRANSACTION)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void failCreateTransactionOnUnauthorizedRequestWebTest() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();
        TransactionRequestDto requestDto = getValidTransactionRequestDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_TRANSACTION)
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }
}

