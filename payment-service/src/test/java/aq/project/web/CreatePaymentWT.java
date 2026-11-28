package aq.project.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.PaymentServiceEntities;
import aq.project.dto.CreatePaymentRequestDto;
import aq.project.entities.PaymentMethod;
import aq.project.entities.PaymentMethodDefinition;
import aq.project.entities.PaymentMethodRequiredField;
import aq.project.repositories.PaymentMethodDefinitionRepository;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentMethodRequiredFieldRepository;
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
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static aq.project.controller.PaymentRestControllerApi.PATH_CREATE_PAYMENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePaymentWT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private PaymentMethodDefinitionRepository paymentMethodDefinitionRepository;

    @Autowired
    private PaymentMethodRequiredFieldRepository paymentMethodRequiredFieldRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void successCreatePayment() throws Exception {
//        Arrange
        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(PaymentServiceEntities.getPaymentMethodDefinition());

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(PaymentServiceEntities.getPaymentMethodRequiredField());

        PaymentMethod newPaymentMethod = PaymentServiceEntities
                .getPaymentMethod(savedPaymentMethodDefinition, savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        CreatePaymentRequestDto createPaymentRequestDto = PaymentServiceEntities.getCreatePaymentRequestDto();

        createPaymentRequestDto.setPaymentMethodId(savedPaymentMethod.getId());
        createPaymentRequestDto.setCurrencyCode(savedPaymentMethod.getRequiredFields().getCurrencyCode());

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String json = objectMapper.writeValueAsString(createPaymentRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    public void failureOnUnauthorizedRequest() throws Exception {
//        Arrange
        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(PaymentServiceEntities.getPaymentMethodDefinition());

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(PaymentServiceEntities.getPaymentMethodRequiredField());

        PaymentMethod newPaymentMethod = PaymentServiceEntities
                .getPaymentMethod(savedPaymentMethodDefinition, savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        CreatePaymentRequestDto createPaymentRequestDto = PaymentServiceEntities.getCreatePaymentRequestDto();

        createPaymentRequestDto.setPaymentMethodId(savedPaymentMethod.getId());
        createPaymentRequestDto.setCurrencyCode(savedPaymentMethod.getRequiredFields().getCurrencyCode());

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String json = objectMapper.writeValueAsString(createPaymentRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_PAYMENT)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}