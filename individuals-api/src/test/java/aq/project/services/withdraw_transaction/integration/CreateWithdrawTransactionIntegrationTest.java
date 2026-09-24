package aq.project.services.withdraw_transaction.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.IndividualsApiServiceWithdrawTransactionRequestDto;
import aq.project.dto.RateResponse;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.transactions.WithdrawTransactionService;
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

import static aq.project._utils.entities.withdraw_transaction_service.WithdrawTransactionServiceEntities.*;

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
public class CreateWithdrawTransactionIntegrationTest {

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
    private WithdrawTransactionService withdrawTransactionService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
        registry.add("application.wallet-service.uri", () -> "http://localhost:18086");
        registry.add("application.currency-rate-service.uri", () -> "http://localhost:18087");
    }

    @Test
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
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateWithdrawTransactionOnWalletServiceErrorResponse() {
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
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateWithdrawTransactionOnCurrencyRateService5xxErrorResponse() {
//        Arrange
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
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateWithdrawTransactionOnCurrencyRateService4xxErrorResponse() {
//        Arrange
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
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateWithdrawTransactionOnTransactionServiceErrorResponse() {
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
                .willReturn(WireMock.serverError()));

        walletServiceMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok(transactionRequestCurrencyCode)));

        currencyRateServiceMock.stubFor(WireMock.get(currencyRateServiceGetRateEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateWithdrawTransactionOnInvalidIndividualsApiServiceWithdrawTransactionRequestDto() {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getInvalidIndividualsApiServiceWithdrawTransactionRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateWithdrawTransactionOnNullIndividualsApiServiceWithdrawTransactionRequestDto() {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());
    }

    @Test
    public void failCreateWithdrawTransactionOnFallback() {
//        Arrange
        final int RATE_LIMIT = 50;

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
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1) {
                new Thread(() -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block()).start();
            } else {
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());
            }
        }
    }
}
