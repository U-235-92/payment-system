package aq.project.services.payment_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PaymentServicePaymentMethodResponseDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.payment_service.PaymentService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import tools.jackson.databind.ObjectMapper;

import static aq.project._utils.entities.PaymentServiceEntities.getValidPaymentServicePaymentMethodResponseDto;
import static aq.project.controller.PaymentRestControllerApi.PATH_GET_PAYMENT_METHODS;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "payment-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetPaymentMethodsIntegrationTest {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("payment-service")
    private WireMockServer paymentServiceMock;

    @Autowired
    private PaymentService paymentService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.payment-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successGetPaymentMethods() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";
        String requestParams = String.format("%s/%s", currencyCode, countryCode);
        String requestUri = PATH_GET_PAYMENT_METHODS.replace("{currencyCode}/{countryCode}", requestParams);

        PaymentServicePaymentMethodResponseDto paymentServicePaymentMethodResponseDto = getValidPaymentServicePaymentMethodResponseDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(paymentServicePaymentMethodResponseDto);

        paymentServiceMock.stubFor(WireMock.get(requestUri).willReturn(WireMock.okJson(json)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.getPaymentMethods(currencyCode, countryCode).block());
    }

    @Test
    public void failGetPaymentMethodsOn4xxPaymentServiceResponse() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";
        String requestParams = String.format("%s/%s", currencyCode, countryCode);
        String requestUri = PATH_GET_PAYMENT_METHODS.replace("{currencyCode}/{countryCode}", requestParams);

        paymentServiceMock.stubFor(WireMock.get(requestUri).willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> paymentService.getPaymentMethods(currencyCode, countryCode).block());
    }

    @Test
    public void failGetPaymentMethodsOn5xxPaymentServiceResponse() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";
        String requestParams = String.format("%s/%s", currencyCode, countryCode);
        String requestUri = PATH_GET_PAYMENT_METHODS.replace("{currencyCode}/{countryCode}", requestParams);

        paymentServiceMock.stubFor(WireMock.get(requestUri).willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> paymentService.getPaymentMethods(currencyCode, countryCode).block());
    }

    @Test
    public void failGetPaymentMethodsOnInvalidRequest() {
//        Arrange
        String currencyCode = "TEST";
        String countryCode = "TEST";

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentService.getPaymentMethods(currencyCode, countryCode).block());
    }

    @Test
    public void failCreateDepositTransactionOnFallback() {
//        Arrange
        final int RATE_LIMIT = 50;

        String currencyCode = "USD";
        String countryCode = "ATA";
        String requestParams = String.format("%s/%s", currencyCode, countryCode);
        String requestUri = PATH_GET_PAYMENT_METHODS.replace("{currencyCode}/{countryCode}", requestParams);

        PaymentServicePaymentMethodResponseDto paymentServicePaymentMethodResponseDto = getValidPaymentServicePaymentMethodResponseDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(paymentServicePaymentMethodResponseDto);

        paymentServiceMock.stubFor(WireMock.get(requestUri).willReturn(WireMock.okJson(json)));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1)
                new Thread(() -> paymentService.getPaymentMethods(currencyCode, countryCode).block()).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> paymentService.getPaymentMethods(currencyCode, countryCode).block());

        }
    }
}
