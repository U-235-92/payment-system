package aq.project.controllers.payment_controller;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.PaymentServicePaymentMethodResponseDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
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
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "payment-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetPaymentMethodsWebTest {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("payment-service")
    private WireMockServer paymentServiceMock;

    @Autowired
    private KeycloakServiceClientFacade keycloakServiceClientFacade;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.payment-service.uri", () -> "http://localhost:18085");
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
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
        webTestClient.get()
                .uri(requestUri)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void successGetPaymentMethodsWithNoMockUser() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";
        String requestParams = String.format("%s/%s", currencyCode, countryCode);
        String requestUri = PATH_GET_PAYMENT_METHODS.replace("{currencyCode}/{countryCode}", requestParams);

        String jwt = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

        PaymentServicePaymentMethodResponseDto paymentServicePaymentMethodResponseDto = getValidPaymentServicePaymentMethodResponseDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(paymentServicePaymentMethodResponseDto);

        paymentServiceMock.stubFor(WireMock.get(requestUri).willReturn(WireMock.okJson(json)));

//        Act & Assert
        webTestClient.get()
                .uri(requestUri)
                .header("Authorization", jwt)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failGetPaymentMethodsOn4xxPaymentServiceResponse() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";
        String requestParams = String.format("%s/%s", currencyCode, countryCode);
        String requestUri = PATH_GET_PAYMENT_METHODS.replace("{currencyCode}/{countryCode}", requestParams);

        paymentServiceMock.stubFor(WireMock.get(requestUri).willReturn(WireMock.badRequest()));

//        Act & Assert
        webTestClient.get()
                .uri(requestUri)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failGetPaymentMethodsOn5xxPaymentServiceResponse() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";
        String requestParams = String.format("%s/%s", currencyCode, countryCode);
        String requestUri = PATH_GET_PAYMENT_METHODS.replace("{currencyCode}/{countryCode}", requestParams);

        paymentServiceMock.stubFor(WireMock.get(requestUri).willReturn(WireMock.serverError()));

//        Act & Assert
        webTestClient.get()
                .uri(requestUri)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failGetPaymentMethodsOnInvalidRequest() {
//        Arrange
        String currencyCode = "TEST";
        String countryCode = "TEST";
        String requestParams = String.format("%s/%s", currencyCode, countryCode);
        String requestUri = PATH_GET_PAYMENT_METHODS.replace("{currencyCode}/{countryCode}", requestParams);

//        Act & Assert
        webTestClient.get()
                .uri(requestUri)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failGetPaymentMethodsOnUnauthorizedRequest() {
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
        webTestClient.get()
                .uri(requestUri)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void failGetPaymentMethodsOnInvalidAuthorization() {
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
        webTestClient.get()
                .uri(requestUri)
                .header("Authorization", "Bearer TEST_JWT")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
