package aq.project.controllers;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.repositories.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Base64;
import java.util.UUID;

import static aq.project._utils.entities.PaymentServiceEntities.*;
import static aq.project.controller.PaymentRestControllerApi.PATH_CREATE_PAYMENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePaymentWebTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void tearDownRepositories() {
        paymentRepository.deleteAll();
        transactionRepository.deleteAll();
        merchantRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test-payment-service", password = "secret", roles = "USER")
    public void successCreatePayment() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();

        Transaction transaction = getValidTransaction();
        transaction.setMerchant(merchant);

        merchantRepository.save(merchant);

        Transaction savedTransaction = transactionRepository.save(transaction);

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(savedTransaction.getId());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(paymentProviderServiceCreatePaymentRequestDto);

        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(xTraceKey, xTraceValue)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-payment-service", password = "secret", roles = "USER")
    public void failCreatePaymentOnInvalidDto() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();

        Transaction transaction = getValidTransaction();
        transaction.setMerchant(merchant);

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getInvalidPaymentProviderServiceCreatePaymentRequestDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(paymentProviderServiceCreatePaymentRequestDto);

        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(xTraceKey, xTraceValue)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "test-payment-service", password = "secret", roles = "TEST")
    public void failCreatePaymentOnInvalidRole() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();

        Transaction transaction = getValidTransaction();
        transaction.setMerchant(merchant);

        merchantRepository.save(merchant);

        Transaction savedTransaction = transactionRepository.save(transaction);

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(savedTransaction.getId());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(paymentProviderServiceCreatePaymentRequestDto);

        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(xTraceKey, xTraceValue)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    public void failCreatePaymentOnInvalidAuthorization() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();
        merchant.setId("test-payment-service");
        merchant.setSecretKey(passwordEncoder.encode("secret"));

        Transaction transaction = getValidTransaction();
        transaction.setMerchant(merchant);

        merchantRepository.save(merchant);

        Transaction savedTransaction = transactionRepository.save(transaction);

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(savedTransaction.getId());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(paymentProviderServiceCreatePaymentRequestDto);

        String keyPass = String.format("%s:%s", "another-test-payment-service", "super-secret");
        String authorization = String.format("%s%s", "Basic ", Base64.getEncoder().encodeToString(keyPass.getBytes()));

        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void failCreatePaymentOnUnauthorizedRequest() throws Exception {
//        Arrange
        Merchant merchant = getValidMerchant();

        Transaction transaction = getValidTransaction();
        transaction.setMerchant(merchant);

        merchantRepository.save(merchant);

        Transaction savedTransaction = transactionRepository.save(transaction);

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(savedTransaction.getId());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(paymentProviderServiceCreatePaymentRequestDto);

        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(xTraceKey, xTraceValue)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    private static String getBase64BasicAuthorizationValue() {
        String keyPass = String.format("%s:%s", "test-payment-service", "secret");
        return String.format("%s%s", "Basic ", Base64.getEncoder().encodeToString(keyPass.getBytes()));
    }
}
