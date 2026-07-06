package aq.project.integration.rate;

import aq.project.dto.RateResponse;
import aq.project.services.RateService;
import aq.project.util.TestApplicationProperties;
import aq.project.util.TestContainers;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "currency-rate-service"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetRateIntegrationTest {

    @Value("${application.currency-rate-service.endpoints.get-rate}")
    private String getRateEndpoint;

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
    public void successGetCurrencyInfoIntegrationTest() {
        final String RUB = "RUB";
        final String USD = "USD";
        final String requestUrl = String.format("%s?from=%s&to=%s", getRateEndpoint, RUB, USD);

        RateResponse rateResponse = new RateResponse();
        rateResponse.setSourceCode(RUB);
        rateResponse.setDestinationCode(USD);
        rateResponse.setRate(BigDecimal.valueOf(1.0));
        rateResponse.setProviderCode("BAM");

        currencyRateServiceMockServer.stubFor(WireMock.get(requestUrl)
                .willReturn(ResponseDefinitionBuilder.okForJson(rateResponse)));

        Assertions.assertDoesNotThrow(() -> rateService.getRate(RUB, USD, null, null));
        Assertions.assertNotNull(rateResponse);
        Assertions.assertEquals(RUB, rateService.getRate(RUB, USD, null, null).block().getSourceCode());
    }

    @Test
    public void failGetCurrencyInfoWithNullCurrencyIntegrationTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> rateService.getRate(null, "EUR", null, null));
    }

    @Test
    public void failGetCurrencyInfoWithInvalidCurrencyIntegrationTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> rateService.getRate("WRONG", "ME", null, null));
    }
}
