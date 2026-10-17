package aq.project.test.controllers;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentProviderServiceErrorDto;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.entities.PaymentMethod;
import aq.project.entities.PaymentMethodDefinition;
import aq.project.entities.PaymentMethodRequiredField;
import aq.project.repositories.PaymentMethodDefinitionRepository;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentMethodRequiredFieldRepository;
import aq.project.repositories.PaymentRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
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
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static aq.project._utils.entities.PaymentServiceEntities.*;
import static aq.project.controller.PaymentRestControllerApi.PATH_CREATE_PAYMENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(name = "payment-provider-mock-service", port = 8558))
public class CreatePaymentWebTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("payment-provider-mock-service")
    private WireMockServer paymentProviderMockService;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentMethodDefinitionRepository paymentMethodDefinitionRepository;
    @Autowired
    private PaymentMethodRequiredFieldRepository paymentMethodRequiredFieldRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("service.payment-provider-service.uri", () -> "http://localhost:8558");
    }

    @AfterEach
    void cleanRepositories() {
        paymentRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        paymentMethodDefinitionRepository.deleteAll();
        paymentMethodRequiredFieldRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test-service", password = "secret")
    public void successCreatePayment() throws Exception {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentMethodDefinition newPaymentMethodDefinition = getValidPaymentMethodDefinition();
        newPaymentMethodDefinition.setId(null);
        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(newPaymentMethodDefinition);

        PaymentMethodRequiredField newPaymentMethodRequiredField = getValidPaymentMethodRequiredField();
        newPaymentMethodRequiredField.setId(null);
        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(newPaymentMethodRequiredField);

        PaymentMethod newPaymentMethod = getValidPaymentMethod();
        newPaymentMethod.setId(null);
        newPaymentMethod.setDefinition(savedPaymentMethodDefinition);
        newPaymentMethod.setRequiredFields(savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        paymentServiceCreatePaymentRequestDto.setTransactionId(transactionId);
        paymentServiceCreatePaymentRequestDto.setPaymentMethodId(savedPaymentMethod.getId());

        PaymentProviderServiceCreatePaymentResponseDto paymentProviderServiceCreatePaymentResponseDto = getValidPaymentProviderServiceCreatePaymentResponseDto();
        paymentProviderServiceCreatePaymentResponseDto.setPaymentId(paymentId);
        paymentProviderServiceCreatePaymentResponseDto.setTransactionId(transactionId);

        String paymentProviderServiceJsonResponse = objectMapper
                .writeValueAsString(paymentProviderServiceCreatePaymentResponseDto);

        String paymentServiceJsonRequest = objectMapper
                .writeValueAsString(paymentServiceCreatePaymentRequestDto);

        paymentProviderMockService.stubFor(WireMock.post(PATH_CREATE_PAYMENT)
                .willReturn(WireMock.okJson(paymentProviderServiceJsonResponse)));

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentServiceJsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-service", password = "secret")
    public void failCreatePaymentOnInvalidRequestDto() throws Exception {
//        Arrange
        PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getInvalidPaymentServiceCreatePaymentRequestDto();

        String paymentServiceJsonRequest = objectMapper
                .writeValueAsString(paymentServiceCreatePaymentRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentServiceJsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-service", password = "secret")
    public void failCreatePaymentOn4xxPaymentProviderResponse() throws Exception {
//        Arrange
        int statusCode = 400;

        UUID transactionId = UUID.randomUUID();

        PaymentMethodDefinition newPaymentMethodDefinition = getValidPaymentMethodDefinition();
        newPaymentMethodDefinition.setId(null);
        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(newPaymentMethodDefinition);

        PaymentMethodRequiredField newPaymentMethodRequiredField = getValidPaymentMethodRequiredField();
        newPaymentMethodRequiredField.setId(null);
        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(newPaymentMethodRequiredField);

        PaymentMethod newPaymentMethod = getValidPaymentMethod();
        newPaymentMethod.setId(null);
        newPaymentMethod.setDefinition(savedPaymentMethodDefinition);
        newPaymentMethod.setRequiredFields(savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        paymentServiceCreatePaymentRequestDto.setTransactionId(transactionId);
        paymentServiceCreatePaymentRequestDto.setPaymentMethodId(savedPaymentMethod.getId());

        PaymentProviderServiceErrorDto errorDto = getPaymentProviderServiceErrorDto();

        String paymentProviderServiceJsonResponse = objectMapper
                .writeValueAsString(errorDto);

        String paymentServiceJsonRequest = objectMapper
                .writeValueAsString(paymentServiceCreatePaymentRequestDto);

        paymentProviderMockService.stubFor(WireMock.post(PATH_CREATE_PAYMENT)
                .willReturn(WireMock.status(statusCode).withBody(paymentProviderServiceJsonResponse)));

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentServiceJsonRequest))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "test-service", password = "secret")
    public void failCreatePaymentOn5xxPaymentProviderResponse() throws Exception {
//        Arrange
        int statusCode = 500;

        UUID transactionId = UUID.randomUUID();

        PaymentMethodDefinition newPaymentMethodDefinition = getValidPaymentMethodDefinition();
        newPaymentMethodDefinition.setId(null);

        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(newPaymentMethodDefinition);

        PaymentMethodRequiredField newPaymentMethodRequiredField = getValidPaymentMethodRequiredField();
        newPaymentMethodRequiredField.setId(null);

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(newPaymentMethodRequiredField);

        PaymentMethod newPaymentMethod = getValidPaymentMethod();
        newPaymentMethod.setId(null);
        newPaymentMethod.setDefinition(savedPaymentMethodDefinition);
        newPaymentMethod.setRequiredFields(savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        paymentServiceCreatePaymentRequestDto.setTransactionId(transactionId);
        paymentServiceCreatePaymentRequestDto.setPaymentMethodId(savedPaymentMethod.getId());

        PaymentProviderServiceErrorDto errorDto = getPaymentProviderServiceErrorDto();

        String paymentProviderServiceJsonResponse = objectMapper
                .writeValueAsString(errorDto);

        String paymentServiceJsonRequest = objectMapper
                .writeValueAsString(paymentServiceCreatePaymentRequestDto);

        paymentProviderMockService.stubFor(WireMock.post(PATH_CREATE_PAYMENT)
                .willReturn(WireMock.status(statusCode).withBody(paymentProviderServiceJsonResponse)));

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentServiceJsonRequest))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void failCreatePaymentOnUnauthorizedRequest() throws Exception {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        Long paymentMethodId = 1L;

        PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        paymentServiceCreatePaymentRequestDto.setTransactionId(transactionId);
        paymentServiceCreatePaymentRequestDto.setPaymentMethodId(paymentMethodId);

        String paymentServiceJsonRequest = objectMapper
                .writeValueAsString(paymentServiceCreatePaymentRequestDto);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentServiceJsonRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void failCreatePaymentOnLakeXTraceIdHeader() throws Exception {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        Long paymentMethodId = 1L;

        PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        paymentServiceCreatePaymentRequestDto.setTransactionId(transactionId);
        paymentServiceCreatePaymentRequestDto.setPaymentMethodId(paymentMethodId);

        String paymentServiceJsonRequest = objectMapper
                .writeValueAsString(paymentServiceCreatePaymentRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentServiceJsonRequest))
                .andExpect(status().isBadRequest());
    }
}
