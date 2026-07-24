package aq.project.integration.rate_service;

import aq.project.clients.FrankfurterRateProviderClient;
import aq.project.entities.Currency;
import aq.project.services.RateService;
import aq.project.utils.mappers.rate.AbstractRateProviderMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpServerErrorException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Map;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@EnableWireMock(value = @ConfigureWireMock(name = "rate-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateCurrencyRatesIntegrationTest {

    @Value("${application.client.frankfurter.endpoint.providers}")
    private String frankfurterProvidersEndpoint;
    @Value("${application.client.frankfurter.endpoint.currencies}")
    private String frankfurterCurrenciesEndpoint;
    @Value("${application.client.frankfurter.endpoint.rates}")
    private String frankfurterRatesEndpoint;

    @Autowired
    private RateService rateService;

    @Autowired
    private AbstractRateProviderMapper rateProviderMapper;

    @Autowired
    private FrankfurterRateProviderClient frankfurterRateProviderClient;

    @InjectWireMock("rate-service-mock")
    private WireMockServer rateServiceWireMockServer;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = new PostgreSQLContainer("postgres:18-alpine");

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("application.client.frankfurter.base-url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successUpdateCurrencyRatesIntegrationTest() throws Exception {
        try(DataInputStream currenciesRateDis = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("src/test/resources/currencies.json")));
            DataInputStream rateProviderDis = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("src/test/resources/rate_providers.json")))
        ) {
            String rateProvidersJsonString = new String(rateProviderDis.readAllBytes());
            String currenciesJsonString = new String(currenciesRateDis.readAllBytes());

            rateServiceWireMockServer.stubFor(WireMock.get(frankfurterProvidersEndpoint)
                    .willReturn(WireMock.ok(rateProvidersJsonString)));
            rateServiceWireMockServer.stubFor(WireMock.get(frankfurterCurrenciesEndpoint)
                    .willReturn(WireMock.ok(currenciesJsonString)));
            Map<String, Currency> currencyMap = rateProviderMapper.getCurrencyMap(frankfurterRateProviderClient.getCurrencies());
            for(String currencyCode : currencyMap.keySet()) {
                String urlEndpoint = frankfurterRatesEndpoint + String.format("?base=%s&expand=providers", currencyCode);
                String mockServiceRateJsonResponse = String.format(
                        "[ {\n" +
                            "  \"date\" : \"2026-07-17\",\n" +
                            "  \"base\" : \"%s\",\n" +
                            "  \"quote\" : \"AED\",\n" +
                            "  \"rate\" : 1.648,\n" +
                            "  \"providers\" : [ {\n" +
                            "    \"key\" : \"BAM\",\n" +
                            "    \"date\" : \"2026-07-15\",\n" +
                            "    \"rate\" : 1.6482,\n" +
                            "    \"excluded\" : true\n" +
                            "  } ]\n" +
                        "} ]", currencyCode);
                rateServiceWireMockServer.stubFor(WireMock.get(urlEndpoint)
                        .willReturn(WireMock.ok(mockServiceRateJsonResponse)));
            }

            Assertions.assertDoesNotThrow(() -> rateService.updateCurrencyRates());
        }
    }

    @Test
    public void failUpdateCurrencyRatesIntegrationTest() {
        rateServiceWireMockServer.stubFor(WireMock.get(frankfurterProvidersEndpoint)
                .willReturn(WireMock.status(500)));
        rateServiceWireMockServer.stubFor(WireMock.get(frankfurterCurrenciesEndpoint)
                .willReturn(WireMock.status(500)));
        rateServiceWireMockServer.stubFor(WireMock.get(frankfurterRatesEndpoint)
                .willReturn(WireMock.status(500)));

        Assertions.assertThrows(HttpServerErrorException.class, () -> rateService.updateCurrencyRates());
    }
}
