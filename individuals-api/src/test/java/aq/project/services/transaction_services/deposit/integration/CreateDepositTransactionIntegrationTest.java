package aq.project.services.transaction_services.deposit.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceDepositTransactionRequestDto;
import aq.project.dto.PaymentServiceCreatePaymentResponseDto;
import aq.project.dto.RateResponse;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.transaction_service.DepositTransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.UUID;

import static aq.project._utils.entities.DepositTransactionServiceEntities.*;
import static aq.project._utils.entities.ErrorEntities.*;
import static aq.project._utils.entities.PaymentServiceEntities.getValidPaymentServiceCreatePaymentResponseDto;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(value = {
        @ConfigureWireMock(name = "transaction-service", port = 18085),
        @ConfigureWireMock(name = "wallet-service", port = 18086),
        @ConfigureWireMock(name = "currency-rate-service", port = 18087),
        @ConfigureWireMock(name = "payment-service", port = 18088)
    }
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateDepositTransactionIntegrationTest {

    @Value("${application.currency-rate-service.endpoints.get-rate}")
    private String currencyRateServiceGetRateEndpoint;
    @Value("${application.wallet-service.endpoints.get-wallet-currency}")
    private String walletServiceGetWalletCurrencyEndpoint;
    @Value("${application.transaction-service.endpoints.create-deposit-transaction}")
    private String transactionServiceCreateDepositTransactionEndpoint;
    @Value("${application.payment-service.endpoints.create-payment}")
    private String paymentServiceCreatePaymentEndpoint;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("transaction-service")
    private WireMockServer transactionServiceMock;
    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;
    @InjectWireMock("currency-rate-service")
    private WireMockServer currencyRateServiceMock;
    @InjectWireMock("payment-service")
    private WireMockServer paymentServiceMock;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepositTransactionService depositTransactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
        registry.add("application.wallet-service.uri", () -> "http://localhost:18086");
        registry.add("application.currency-rate-service.uri", () -> "http://localhost:18087");
        registry.add("application.payment-service.uri", () -> "http://localhost:18088");
    }

    @Test
    public void successCreateDepositTransaction() {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        PaymentServiceCreatePaymentResponseDto paymentServiceCreatePaymentResponseDto = getValidPaymentServiceCreatePaymentResponseDto();
        
        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        paymentServiceMock.stubFor(WireMock.post(paymentServiceCreatePaymentEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(paymentServiceCreatePaymentResponseDto)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnWalletService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String walletServiceErrorDtoJson = objectMapper.writeValueAsString(getWalletServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(walletServiceErrorDtoJson)));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnWalletService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String walletServiceErrorDtoJson = objectMapper.writeValueAsString(getWalletServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(walletServiceErrorDtoJson)));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnCurrencyRateService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String currencyRateServiceErrorDtoJson = objectMapper.writeValueAsString(getCurrencyRateServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(currencyRateServiceErrorDtoJson)));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnCurrencyRateService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String currencyRateServiceErrorDtoJson = objectMapper.writeValueAsString(getCurrencyRateServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(currencyRateServiceErrorDtoJson)));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnTransactionService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String transactionServiceErrorDtoJson = objectMapper.writeValueAsString(getTransactionServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnTransactionService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String transactionServiceErrorDtoJson = objectMapper.writeValueAsString(getTransactionServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnPaymentService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String transactionServiceErrorDtoJson = objectMapper.writeValueAsString(getTransactionServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnPaymentService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String transactionServiceErrorDtoJson = objectMapper.writeValueAsString(getTransactionServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnInvalidIndividualsApiServiceDepositTransactionRequestDto() {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getInvalidIndividualsApiServiceDepositTransactionRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnNullIndividualsApiServiceDepositTransactionRequestDto() {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateDepositTransactionOnFallback() {
//        Arrange
        final int RATE_LIMIT = 50;

        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1) {
                new Thread(() -> depositTransactionService.createDepositTransaction(transactionRequestDto).block()).start();
            } else {
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());
            }
        }
    }
}
