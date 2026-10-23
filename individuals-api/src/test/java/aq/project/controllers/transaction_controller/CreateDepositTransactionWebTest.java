package aq.project.controllers.transaction_controller;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.IndividualsApiServiceDepositTransactionRequestDto;
import aq.project.dto.PaymentServiceCreatePaymentResponseDto;
import aq.project.dto.RateResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
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

import static aq.project._utils.entities.DepositTransactionServiceEntities.*;
import static aq.project._utils.entities.ErrorEntities.*;
import static aq.project._utils.entities.PaymentServiceEntities.getValidPaymentServiceCreatePaymentResponseDto;
import static aq.project.controller.DepositTransactionRestControllerApi.PATH_CREATE_DEPOSIT_TRANSACTION;

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
public class CreateDepositTransactionWebTest {

    @Value("${application.currency-rate-service.endpoints.get-rate}")
    private String currencyRateServiceGetRateEndpoint;
    @Value("${application.wallet-service.endpoints.get-wallet-currency}")
    private String walletServiceGetWalletCurrencyEndpoint;
    @Value("${application.transaction-service.endpoints.create-deposit-transaction}")
    private String transactionServiceCreateDepositTransactionEndpoint;
    @Value("${application.transaction-service.endpoints.fail-deposit-transaction}")
    private String transactionServiceFailDepositTransactionEndpoint;
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

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        paymentServiceMock.stubFor(WireMock.post(paymentServiceCreatePaymentEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(paymentServiceCreatePaymentResponseDto)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void successCreateDepositTransactionWithNoMockUser() {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        PaymentServiceCreatePaymentResponseDto paymentServiceCreatePaymentResponseDto = getValidPaymentServiceCreatePaymentResponseDto();

        String jwt = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        paymentServiceMock.stubFor(WireMock.post(paymentServiceCreatePaymentEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(paymentServiceCreatePaymentResponseDto)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
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
    public void failCreateDepositTransactionOnWalletService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String walletServiceErrorDtoJson = objectMapper.writeValueAsString(getWalletServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(walletServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateDepositTransactionOnWalletService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        String walletServiceErrorDtoJson = objectMapper.writeValueAsString(getWalletServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(walletServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
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

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(currencyRateServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
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

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(currencyRateServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
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

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
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

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(transactionServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateDepositTransactionOnPaymentService4xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        RateResponse rateResponse = getValidRateResponse();

        String paymentServiceErrorDtoJson = objectMapper.writeValueAsString(getPaymentServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        transactionServiceMock.stubFor(WireMock.post(new UrlPattern(WireMock.containing(transactionServiceFailDepositTransactionEndpoint), false))
                .willReturn(WireMock.ok()));

        paymentServiceMock.stubFor(WireMock.post(paymentServiceCreatePaymentEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateDepositTransactionOnPaymentService5xxErrorResponse() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        RateResponse rateResponse = getValidRateResponse();

        String paymentServiceErrorDtoJson = objectMapper.writeValueAsString(getPaymentServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        transactionServiceMock.stubFor(WireMock.post(new UrlPattern(WireMock.containing(transactionServiceFailDepositTransactionEndpoint), false))
                .willReturn(WireMock.ok()));

        paymentServiceMock.stubFor(WireMock.post(paymentServiceCreatePaymentEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateDepositTransactionOnPaymentServiceErrorResponseAndTransactionService4xxErrorResponseOnFailTransaction() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        RateResponse rateResponse = getValidRateResponse();

        String paymentServiceErrorDtoJson = objectMapper.writeValueAsString(getPaymentServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        transactionServiceMock.stubFor(WireMock.post(new UrlPattern(WireMock.containing(transactionServiceFailDepositTransactionEndpoint), false))
                .willReturn(WireMock.badRequest()));

        paymentServiceMock.stubFor(WireMock.post(paymentServiceCreatePaymentEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateDepositTransactionOnPaymentServiceErrorResponseAndTransactionService5xxErrorResponseOnFailTransaction() throws JsonProcessingException {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        RateResponse rateResponse = getValidRateResponse();

        String paymentServiceErrorDtoJson = objectMapper.writeValueAsString(getPaymentServiceErrorDto());

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateDepositTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        transactionServiceMock.stubFor(WireMock.post(new UrlPattern(WireMock.containing(transactionServiceFailDepositTransactionEndpoint), false))
                .willReturn(WireMock.serverError()));

        paymentServiceMock.stubFor(WireMock.post(paymentServiceCreatePaymentEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentServiceErrorDtoJson)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateDepositTransactionOnInvalidIndividualsApiServiceDepositTransactionRequestDto() {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getInvalidIndividualsApiServiceDepositTransactionRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failCreateDepositTransactionOnUnauthorizedRequest() {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void failCreateDepositTransactionOnInvalidAuthorization() {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_DEPOSIT_TRANSACTION)
                .header("Authorization", "Bearer TEST_JWT")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .isUnauthorized();

    }
}
