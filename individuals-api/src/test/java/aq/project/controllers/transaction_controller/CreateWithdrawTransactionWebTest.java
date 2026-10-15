package aq.project.controllers.transaction_controller;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.WithdrawTransactionServiceEntities;
import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.IndividualsApiServiceTransferTransactionRequestDto;
import aq.project.dto.IndividualsApiServiceWithdrawTransactionRequestDto;
import aq.project.dto.PaymentServiceCreatePaymentResponseDto;
import aq.project.dto.RateResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
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

import java.util.UUID;

import static aq.project._utils.entities.ErrorEntities.*;
import static aq.project._utils.entities.PaymentServiceEntities.getValidPaymentServiceCreatePaymentResponseDto;
import static aq.project._utils.entities.TransferTransactionServiceEntities.getValidIndividualsApiServiceTransferTransactionRequestDto;
import static aq.project._utils.entities.TransferTransactionServiceEntities.getValidRateResponse;
import static aq.project._utils.entities.WithdrawTransactionServiceEntities.getInvalidIndividualsApiServiceWithdrawTransactionRequestDto;
import static aq.project._utils.entities.WithdrawTransactionServiceEntities.getValidIndividualsApiServiceWithdrawTransactionRequestDto;
import static aq.project.controller.TransferTransactionRestControllerApi.PATH_CREATE_TRANSFER_TRANSACTION;
import static aq.project.controller.WithdrawTransactionRestControllerApi.PATH_CREATE_WITHDRAW_TRANSACTION;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(value = {
        @ConfigureWireMock(name = "transaction-service", port = 18085),
        @ConfigureWireMock(name = "wallet-service", port = 18086),
        @ConfigureWireMock(name = "currency-rate-service", port = 18087),
        @ConfigureWireMock(name = "payment-service", port = 18088)
    }
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateWithdrawTransactionWebTest {

    @Value("${application.currency-rate-service.endpoints.get-rate}")
    private String currencyRateServiceGetRateEndpoint;
    @Value("${application.wallet-service.endpoints.get-wallet-currency}")
    private String walletServiceGetWalletCurrencyEndpoint;
    @Value("${application.transaction-service.endpoints.create-withdraw-transaction}")
    private String transactionServiceCreateWithdrawTransactionEndpoint;
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
    private KeycloakServiceClientFacade keycloakServiceClientFacade;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
        registry.add("application.wallet-service.uri", () -> "http://localhost:18086");
        registry.add("application.currency-rate-service.uri", () -> "http://localhost:18087");
        registry.add("application.payment-service.uri", () -> "http://localhost:18088");
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void successCreateWithdrawTransaction() {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        PaymentServiceCreatePaymentResponseDto paymentServiceCreatePaymentResponseDto = getValidPaymentServiceCreatePaymentResponseDto();

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        paymentServiceMock.stubFor(WireMock.post(paymentServiceCreatePaymentEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(paymentServiceCreatePaymentResponseDto)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void successCreateWithdrawTransactionWithNoMockUser() {
//        Arrange
        RateResponse rateResponse = WithdrawTransactionServiceEntities.getValidRateResponse();

        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        PaymentServiceCreatePaymentResponseDto paymentServiceCreatePaymentResponseDto = getValidPaymentServiceCreatePaymentResponseDto();

        String jwt = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        paymentServiceMock.stubFor(WireMock.post(paymentServiceCreatePaymentEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(paymentServiceCreatePaymentResponseDto)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", jwt)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOnWalletService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String walletServiceErrorDtoJson = objectMapper.writeValueAsString(getWalletServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(walletServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOnWalletService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String walletServiceErrorDtoJson = objectMapper.writeValueAsString(getWalletServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(walletServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOnCurrencyRateService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String currencyRateServiceErrorDtoJson = objectMapper.writeValueAsString(getCurrencyRateServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(currencyRateServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOnCurrencyRateService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String currencyRateServiceErrorDtoJson = objectMapper.writeValueAsString(getCurrencyRateServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(currencyRateServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOnTransactionService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String transactionServiceErrorDtoJson = objectMapper.writeValueAsString(getTransactionServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOnTransactionService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String transactionServiceErrorDtoJson = objectMapper.writeValueAsString(getTransactionServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOnPaymentService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String transactionServiceErrorDtoJson = objectMapper.writeValueAsString(getTransactionServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOnPaymentService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String transactionServiceErrorDtoJson = objectMapper.writeValueAsString(getTransactionServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOnInvalidIndividualsApiServiceWithdrawTransactionRequestDto() {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getInvalidIndividualsApiServiceWithdrawTransactionRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failCreateWithdrawTransactionOnUnauthorizedRequest() {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WITHDRAW_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void failCreateTransferTransactionOnInvalidAuthorization() {
//        Arrange
        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceTransferTransactionRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_TRANSFER_TRANSACTION)
                .header("Authorization", "Bearer TEST_JWT")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .isUnauthorized();

    }
}
