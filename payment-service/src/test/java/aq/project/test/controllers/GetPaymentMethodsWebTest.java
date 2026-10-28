package aq.project.test.controllers;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.PaymentMethod;
import aq.project.entities.PaymentMethodDefinition;
import aq.project.entities.PaymentMethodRequiredField;
import aq.project.repositories.PaymentMethodDefinitionRepository;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentMethodRequiredFieldRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.UUID;

import static aq.project._utils.entities.PaymentServiceEntities.*;
import static aq.project.controller.PaymentRestControllerApi.PATH_GET_PAYMENT_METHODS;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(name = "payment-provider-mock-service", port = 8558))
public class GetPaymentMethodsWebTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("payment-provider-mock-service")
    private WireMockServer paymentProviderMockService;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentMethodDefinitionRepository paymentMethodDefinitionRepository;
    @Autowired
    private PaymentMethodRequiredFieldRepository paymentMethodRequiredFieldRepository;

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
        paymentMethodRepository.deleteAll();
        paymentMethodDefinitionRepository.deleteAll();
        paymentMethodRequiredFieldRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test-service", password = "secret")
    public void successGetPaymentMethods() throws Exception {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        PaymentMethodDefinition newPaymentMethodDefinition = getValidPaymentMethodDefinition();
        newPaymentMethodDefinition.setId(null);

        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(newPaymentMethodDefinition);

        PaymentMethodRequiredField newPaymentMethodRequiredField = getValidPaymentMethodRequiredField();
        newPaymentMethodRequiredField.setId(null);
        newPaymentMethodRequiredField.setCurrencyCode(currencyCode);
        newPaymentMethodRequiredField.setCountryAlpha3Code(countryCode);

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(newPaymentMethodRequiredField);

        PaymentMethod newPaymentMethod = getValidPaymentMethod();
        newPaymentMethod.setId(null);
        newPaymentMethod.setDefinition(savedPaymentMethodDefinition);
        newPaymentMethod.setRequiredFields(savedPaymentMethodRequiredField);

        paymentMethodRepository.save(newPaymentMethod);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String getPaymentMethodsUri = PATH_GET_PAYMENT_METHODS
                .replace("{currencyCode}/{countryCode}", String.format("%s/%s", currencyCode, countryCode));

//        Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get(getPaymentMethodsUri)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-service", password = "secret")
    public void successGetPaymentMethodsOnEmptyPaymentMethodList() throws Exception {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String getPaymentMethodsUri = PATH_GET_PAYMENT_METHODS
                .replace("{currencyCode}/{countryCode}", String.format("%s/%s", currencyCode, countryCode));

//        Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get(getPaymentMethodsUri)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-service", password = "secret")
    public void failGetPaymentMethodsOnInvalidArguments() throws Exception {
//        Arrange
        String currencyCode = "A";
        String countryCode = "B";

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String getPaymentMethodsUri = PATH_GET_PAYMENT_METHODS
                .replace("{currencyCode}/{countryCode}", String.format("%s/%s", currencyCode, countryCode));

//        Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get(getPaymentMethodsUri)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-service", password = "secret")
    public void failGetPaymentMethodsOnBlankArguments() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String getPaymentMethodsUri = PATH_GET_PAYMENT_METHODS
                .replace("{currencyCode}/{countryCode}", "");

//        Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get(getPaymentMethodsUri)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isForbidden());
    }

    @Test
    public void failCreatePaymentOnUnauthorizedRequest() throws Exception {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String getPaymentMethodsUri = PATH_GET_PAYMENT_METHODS
                .replace("{currencyCode}/{countryCode}", String.format("%s/%s", currencyCode, countryCode));

//        Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get(getPaymentMethodsUri)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void failCreatePaymentOnLakeXTraceIdHeader() throws Exception {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        String getPaymentMethodsUri = PATH_GET_PAYMENT_METHODS
                .replace("{currencyCode}/{countryCode}", String.format("%s/%s", currencyCode, countryCode));

//        Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get(getPaymentMethodsUri))
                .andExpect(status().isBadRequest());
    }
}
