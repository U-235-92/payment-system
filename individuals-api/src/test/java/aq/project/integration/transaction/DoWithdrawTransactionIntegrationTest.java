package aq.project.integration.transaction;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.ErrorDto;
import aq.project.dto.RateResponse;
import aq.project.dto.TransactionRequestDto;
import aq.project.services.TransactionService;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import aq.project.utils.constants.RequestPropertyKeys;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static aq.project.dto.TransactionRequestDto.OperationTypeEnum.WITHDRAW;
import static aq.project.utils.constants.RequestPropertyKeys.RECIPIENT_WALLET_ID;

@Testcontainers
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(value = {
        @ConfigureWireMock(name = "transaction-service", port = 18085),
        @ConfigureWireMock(name = "wallet-service", port = 18086),
        @ConfigureWireMock(name = "currency-rate-service", port = 18087)
    }
)
public class DoWithdrawTransactionIntegrationTest {

    @Value("${application.transaction-service.endpoints.send-transaction-request}")
    private String sendTransactionRequestUri;
    @Value("${application.wallet-service.endpoints.get-wallet-currency}")
    private String walletServiceGetWalletCurrencyEndpoint;
    @Value("${application.currency-rate-service.endpoints.get-rate}")
    private String getRateEndpoint;
    @Value("${application.individuals-api.endpoints.do-transaction}")
    private String individualsApiDoDepositTransactionEndpoint;

    @Autowired
    private KeycloakServiceClientFacade keycloakServiceClientFacade;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TransactionService transactionService;

    @InjectWireMock("transaction-service")
    private WireMockServer transactionServiceMock;
    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;
    @InjectWireMock("currency-rate-service")
    private WireMockServer currencyRateServiceMock;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties.registerApplicationContextContainerProperties(registry);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
        registry.add("application.wallet-service.uri", () -> "http://localhost:18086");
        registry.add("application.currency-rate-service.uri", () -> "http://localhost:18087");
    }

    @Test
    public void successDoTransactionRequestDtoTest() {
//        Prepare constants
        String RUB = "RUB";
        String transactionIdResponse = UUID.randomUUID().toString();

//        Prepare DTO
        TransactionRequestDto transactionRequestDTO = getValidWithdrawTransactionRequestDto();
        RateResponse rateResponse = getValidRateResponse();

//        Prepare URL
        String getWalletCurrencyUrl = String.format("%s/%s",
                walletServiceGetWalletCurrencyEndpoint, transactionRequestDTO.getProperties().get(RECIPIENT_WALLET_ID));
        String getRateUrl = String.format("%s?from=%s&to=%s", getRateEndpoint, RUB, RUB);

//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.post(sendTransactionRequestUri)
                .willReturn(WireMock.ok(transactionIdResponse)));
        walletServiceMock.stubFor(WireMock.get(getWalletCurrencyUrl)
                .willReturn(WireMock.ok(RUB)));
        currencyRateServiceMock.stubFor(WireMock.get(getRateUrl)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Test call
        Assertions.assertDoesNotThrow(() -> transactionService.doTransaction(transactionRequestDTO).block());
    }

    @Test
    public void failOn5xxStatusTransactionServiceResponseTest() {
//        Prepare constants
        String RUB = "RUB";

//        Prepare DTO
        TransactionRequestDto transactionRequestDTO = getValidWithdrawTransactionRequestDto();
        RateResponse rateResponse = getValidRateResponse();

//        Prepare URL
        String getWalletCurrencyUrl = String.format("%s/%s",
                walletServiceGetWalletCurrencyEndpoint, transactionRequestDTO.getProperties().get(RECIPIENT_WALLET_ID));
        String getRateUrl = String.format("%s?from=%s&to=%s", getRateEndpoint, RUB, RUB);

//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.post(sendTransactionRequestUri)
                .willReturn(WireMock.status(500)));
        walletServiceMock.stubFor(WireMock.get(getWalletCurrencyUrl)
                .willReturn(WireMock.ok(RUB)));
        currencyRateServiceMock.stubFor(WireMock.get(getRateUrl)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Prepare test resources
        String adminJwtBearer = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

//        Test call
        webTestClient.post()
                .uri(individualsApiDoDepositTransactionEndpoint)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearer)
                .bodyValue(transactionRequestDTO)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorDto.class);
    }

    @Test
    public void failOn4xxStatusTransactionServiceResponseTest() {
//        Prepare constants
        String RUB = "RUB";

//        Prepare DTO
        TransactionRequestDto transactionRequestDTO = getValidWithdrawTransactionRequestDto();
        RateResponse rateResponse = getValidRateResponse();

//        Prepare URL
        String getWalletCurrencyUrl = String.format("%s/%s",
                walletServiceGetWalletCurrencyEndpoint, transactionRequestDTO.getProperties().get(RECIPIENT_WALLET_ID));
        String getRateUrl = String.format("%s?from=%s&to=%s", getRateEndpoint, RUB, RUB);

//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.post(sendTransactionRequestUri)
                .willReturn(WireMock.status(400)));
        walletServiceMock.stubFor(WireMock.get(getWalletCurrencyUrl)
                .willReturn(WireMock.ok(RUB)));
        currencyRateServiceMock.stubFor(WireMock.get(getRateUrl)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

//        Prepare test resources
        String adminJwtBearer = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

//        Test call
        webTestClient.post()
                .uri(individualsApiDoDepositTransactionEndpoint)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearer)
                .bodyValue(transactionRequestDTO)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDto.class);
    }

    @Test
    public void failOnInvalidTransactionRequestDtoTest() {
//        Prepare mock service
        transactionServiceMock.stubFor(WireMock.post(sendTransactionRequestUri)
                .willReturn(WireMock.status(400)));

//        Prepare test resources
        String adminJwtBearer = keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue().block();

//        Test call
        webTestClient.post()
                .uri(individualsApiDoDepositTransactionEndpoint)
                .header(HttpHeaders.AUTHORIZATION, adminJwtBearer)
                .bodyValue(getInvalidWithdrawTransactionRequestDto())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDto.class);
    }

    private TransactionRequestDto getValidWithdrawTransactionRequestDto() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RequestPropertyKeys.RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        return new TransactionRequestDto()
                .operationType(WITHDRAW)
                .amount("85.58")
                .currency("RUB")
                .timestamp(System.currentTimeMillis())
                .properties(properties);
    }

    private TransactionRequestDto getInvalidWithdrawTransactionRequestDto() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RequestPropertyKeys.RECIPIENT_WALLET_ID, "invalid-id");
        return new TransactionRequestDto()
                .operationType(WITHDRAW)
                .amount("-85.58")
                .currency("HELLO")
                .timestamp(-System.currentTimeMillis())
                .properties(properties);
    }

    private RateResponse getValidRateResponse() {
        final String RUB = "RUB";
        RateResponse rateResponse = new RateResponse();
        rateResponse.setRate(BigDecimal.valueOf(1.00));
        rateResponse.setProviderCode("CBR");
        rateResponse.setSourceCode(RUB);
        rateResponse.setDestinationCode(RUB);
        rateResponse.setRateDate(OffsetDateTime.now());
        return rateResponse;
    }
}
