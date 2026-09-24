package aq.project.controllers.transaction;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceTransferTransactionRequestDto;
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

import static aq.project._utils.entities.transfer_transaction_service.TransferTransactionServiceEntities.*;
import static aq.project.controller.TransferTransactionRestControllerApi.*;

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
public class CreateTransferTransactionWebTest {

    @Value("${application.currency-rate-service.endpoints.get-rate}")
    private String currencyRateServiceGetRateEndpoint;

    @Value("${application.wallet-service.endpoints.get-wallet-currency}")
    private String walletServiceGetWalletCurrencyEndpoint;

    @Value("${application.transaction-service.endpoints.create-transfer-transaction}")
    private String transactionServiceCreateTransferTransactionEndpoint;

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
    public void successCreateTransferTransaction() {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceTransferTransactionRequestDto();

        String transactionRequestSenderWalletId = transactionRequestDto.getSenderWalletId().toString();
        String transactionRequestRecipientWalletId = transactionRequestDto.getRecipientWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetSenderWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestSenderWalletId);
        String walletServiceGetRecipientWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestRecipientWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateTransferTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetSenderWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));
        walletServiceMock.stubFor(WireMock.get(walletServiceGetRecipientWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_TRANSFER_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateTransferTransactionOn5xxInternalServiceResponse() {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceTransferTransactionRequestDto();

        String transactionRequestSenderWalletId = transactionRequestDto.getSenderWalletId().toString();
        String transactionRequestRecipientWalletId = transactionRequestDto.getRecipientWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetSenderWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestSenderWalletId);
        String walletServiceGetRecipientWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestRecipientWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateTransferTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetSenderWalletCurrencyEndpoint)
                .willReturn(WireMock.serverError()));
        walletServiceMock.stubFor(WireMock.get(walletServiceGetRecipientWalletCurrencyEndpoint)
                .willReturn(WireMock.serverError()));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_TRANSFER_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateTransferTransactionOn4xxInternalServiceResponse() {
//        Arrange
        RateResponse rateResponse = getValidRateResponse();

        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceTransferTransactionRequestDto();

        String transactionRequestSenderWalletId = transactionRequestDto.getSenderWalletId().toString();
        String transactionRequestRecipientWalletId = transactionRequestDto.getRecipientWalletId().toString();
        String transactionRequestCurrencyCode = transactionRequestDto.getCurrencyCode();

        String walletServiceGetSenderWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestSenderWalletId);
        String walletServiceGetRecipientWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, transactionRequestRecipientWalletId);
        String currencyRateServiceGetRateEndpoint = String.format(
                "%s?from=%s&to=%s", this.currencyRateServiceGetRateEndpoint, transactionRequestCurrencyCode, transactionRequestCurrencyCode);

        transactionServiceMock.stubFor(WireMock.post(transactionServiceCreateTransferTransactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetSenderWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));
        walletServiceMock.stubFor(WireMock.get(walletServiceGetRecipientWalletCurrencyEndpoint)
                .willReturn(WireMock.badRequest()));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_TRANSFER_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateTransferTransactionOnInvalidIndividualsApiServiceTransferTransactionRequestDto() {
//        Arrange
        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getInvalidIndividualsApiServiceTransferTransactionRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_TRANSFER_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failCreateTransferTransactionOnUnauthorizedRequest() {
//        Arrange
        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceTransferTransactionRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_TRANSFER_TRANSACTION)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(transactionRequestDto)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
