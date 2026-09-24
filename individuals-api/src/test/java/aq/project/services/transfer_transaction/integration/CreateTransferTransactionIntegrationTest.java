package aq.project.services.transfer_transaction.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceTransferTransactionRequestDto;
import aq.project.dto.RateResponse;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.transactions.TransferTransactionService;
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

import static aq.project._utils.entities.transfer_transaction_service.TransferTransactionServiceEntities.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(value = {
        @ConfigureWireMock(name = "transaction-service", port = 18085),
        @ConfigureWireMock(name = "wallet-service", port = 18086),
        @ConfigureWireMock(name = "currency-rate-service", port = 18087)
    }
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateTransferTransactionIntegrationTest {

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
    private TransferTransactionService transferTransactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
        registry.add("application.wallet-service.uri", () -> "http://localhost:18086");
        registry.add("application.currency-rate-service.uri", () -> "http://localhost:18087");
    }

    @Test
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
        Assertions.assertDoesNotThrow(() -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateTransferTransactionOnWalletServiceErrorResponse() {
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
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateTransferTransactionOnCurrencyRateService5xxErrorResponse() {
//        Arrange
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
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateTransferTransactionOnCurrencyRateService4xxErrorResponse() {
//        Arrange
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
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateTransferTransactionOnTransactionServiceErrorResponse() {
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
                .willReturn(WireMock.serverError()));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetSenderWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));
        walletServiceMock.stubFor(WireMock.get(walletServiceGetRecipientWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateTransferTransactionOnInvalidIndividualsApiServiceTransferTransactionRequestDto() {
//        Arrange
        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getInvalidIndividualsApiServiceTransferTransactionRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateTransferTransactionOnNullIndividualsApiServiceTransferTransactionRequestDto() {
//        Arrange
        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateTransferTransactionOnFallback() {
//        Arrange
        final int RATE_LIMIT = 50;

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
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1) {
                new Thread(() -> transferTransactionService.createTransferTransaction(transactionRequestDto).block()).start();
            } else {
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());
            }
        }
    }
}
