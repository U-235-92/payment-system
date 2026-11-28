package aq.project.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
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

import static aq.project.controller.PaymentRestControllerApi.PATH_GET_PAYMENT_METHODS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetPaymentMethodsWT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void successGetPaymentMethods() throws Exception {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String getPaymentMethodsEndpoint = PATH_GET_PAYMENT_METHODS
                .replace("{currencyCode}/{countryCode}", currencyCode + "/" + countryCode);

//        Act & Assert
        mockMvc.perform(get(getPaymentMethodsEndpoint)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    @Test
    public void failureOnUnauthorizedRequest() throws Exception {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String getPaymentMethodsEndpoint = PATH_GET_PAYMENT_METHODS
                .replace("{currencyCode}/{countryCode}", currencyCode + "/" + countryCode);

//        Act & Assert
        mockMvc.perform(get(getPaymentMethodsEndpoint)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isUnauthorized());
    }
}