package aq.project.integration.rate;

import aq.project.dto.CurrencyResponse;
import aq.project.services.RateService;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.List;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "currency-rate-service"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetCurrenciesIntegrationTest {

    @Value("${application.currency-rate-service.endpoints.get-currencies}")
    private String getCurrenciesEndpoint;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @InjectWireMock("currency-rate-service")
    private WireMockServer currencyRateServiceMockServer;

    @Autowired
    private RateService rateService;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties.registerApplicationContextContainerProperties(registry);
        registry.add("application.currency-rate-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successGetCurrenciesIntegrationTest() {
        CurrencyResponse rubCurrencyResponse = new CurrencyResponse();
        rubCurrencyResponse.setCode("RUB");
        rubCurrencyResponse.setIsoCode(800);
        rubCurrencyResponse.setDescription("Description");
        rubCurrencyResponse.setActive(true);
        rubCurrencyResponse.setSymbol("R");

        CurrencyResponse usdCurrencyResponse = new CurrencyResponse();
        usdCurrencyResponse.setCode("USD");
        usdCurrencyResponse.setIsoCode(900);
        usdCurrencyResponse.setDescription("Description");
        usdCurrencyResponse.setActive(true);
        usdCurrencyResponse.setSymbol("U");

        currencyRateServiceMockServer.stubFor(WireMock.get(getCurrenciesEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(List.of(rubCurrencyResponse, usdCurrencyResponse))));

        CurrencyResponse currencyResponse = rateService.getCurrencies().block().blockFirst();

        Assertions.assertDoesNotThrow(() -> rateService.getCurrencies());
        Assertions.assertNotNull(currencyResponse);
        Assertions.assertEquals("RUB", currencyResponse.getCode());
    }
}
