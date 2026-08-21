package aq.project.payment_service_handler.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.messages.TransactionRequest;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.telemetry.TraceContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.security.SecureRandom;

import static aq.project._utils.Entities.getInvalidTransactionRequest;
import static aq.project._utils.Entities.getValidTransactionRequest;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "payment-provider-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SendTransactionRequestToPaymentServiceIntegrationTest {

    private static final String PAYMENT_SERVICE_CREATE_TRANSACTION_ENDPOINT = "/api/v1/transactions/create";

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;
    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private PaymentServiceHandler paymentServiceHandler;

    @Autowired
    private TraceContext traceContext;

    @InjectWireMock("payment-provider-service-mock")
    private WireMockServer paymentProviderServiceMockServer;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        registry.add("service.payment-provider-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @BeforeEach
    public void propagateTraceId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder xTraceId = new StringBuilder();
        for(byte b : bytes) {
            xTraceId.append(String.format("%02x", b));
        }
        traceContext.setTraceId(xTraceId.toString());
    }

    @AfterEach
    public void cleanTraceContext() {
        traceContext.clean();
    }

    @Test
    public void successSendTransactionRequestToPaymentServiceIntegrationTest() {
//        Arrange
        TransactionRequest transactionRequest = getValidTransactionRequest();

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_CREATE_TRANSACTION_ENDPOINT)
                .willReturn(WireMock.created()));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentServiceHandler.sendTransactionRequestToPaymentService(transactionRequest));
    }

    @Test
    public void failSendTransactionRequestToPaymentServiceOn4xxErrorResponseIntegrationTest() {
//        Arrange
        TransactionRequest transactionRequest = getValidTransactionRequest();

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_CREATE_TRANSACTION_ENDPOINT)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(ClientHttpException.class,
                () -> paymentServiceHandler.sendTransactionRequestToPaymentService(transactionRequest));
    }

    @Test
    public void failSendTransactionRequestToPaymentServiceOn5xxErrorResponseAndSuccessFallbackCallIntegrationTest() {
//        Arrange
        TransactionRequest transactionRequest = getValidTransactionRequest();

        paymentProviderServiceMockServer.stubFor(WireMock.post(PAYMENT_SERVICE_CREATE_TRANSACTION_ENDPOINT)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> paymentServiceHandler.sendTransactionRequestToPaymentService(transactionRequest));
    }

    @Test
    public void failSendTransactionRequestToPaymentServiceOnNullTransactionRequestIntegrationTest() {
//        Arrange
        TransactionRequest transactionRequest = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentServiceHandler.sendTransactionRequestToPaymentService(transactionRequest));
    }

    @Test
    public void failSendTransactionRequestToPaymentServiceOnInvalidTransactionRequestIntegrationTest() {
//        Arrange
        TransactionRequest transactionRequest = getInvalidTransactionRequest();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentServiceHandler.sendTransactionRequestToPaymentService(transactionRequest));
    }
}
