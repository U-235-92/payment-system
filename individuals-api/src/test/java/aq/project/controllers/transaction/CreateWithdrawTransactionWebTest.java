package aq.project.controllers.transaction;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceWithdrawTransactionRequestDto;
import aq.project.dto.RateResponse;
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

import static aq.project._utils.entities.withdraw_transaction_service.WithdrawTransactionServiceEntities.*;
import static aq.project.controller.WithdrawTransactionRestControllerApi.PATH_CREATE_WITHDRAW_TRANSACTION;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(value = {
        @ConfigureWireMock(name = "transaction-service", port = 18085),
        @ConfigureWireMock(name = "wallet-service", port = 18086),
        @ConfigureWireMock(name = "currency-rate-service", port = 18087)
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

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("transaction-service")
    private WireMockServer transactionServiceMock;
    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;
    @InjectWireMock("currency-rate-service")
    private WireMockServer currencyRateServiceMock;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
        registry.add("application.wallet-service.uri", () -> "http://localhost:18086");
        registry.add("application.currency-rate-service.uri", () -> "http://localhost:18087");
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void successCreateWithdrawTransaction() {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

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
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWithdrawTransactionOn5xxInternalServiceResponse() {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.serverError()));

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
    public void failCreateWithdrawTransactionOn4xxInternalServiceResponse() {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        String transactionRequestWalletId = transactionRequestDto.getWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateWithdrawTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.badRequest()));

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
}
