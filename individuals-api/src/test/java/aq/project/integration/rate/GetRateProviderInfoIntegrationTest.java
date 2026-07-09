package aq.project.integration.rate;

import aq.project.dto.RateProviderResponse;
import aq.project.services.CurrencyRateService;
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

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "currency-rate-service"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetRateProviderInfoIntegrationTest {

    @Value("${application.currency-rate-service.endpoints.get-rate-provider}")
    private String getRateProviderEndpoint;

    @Container
    private static final KeycloakContainer KEYCLOAK = TestContainers.Keycloak.KEYCLOAK_CONTAINER;

    @InjectWireMock("currency-rate-service")
    private WireMockServer currencyRateServiceMockServer;

    @Autowired
    private CurrencyRateService currencyRateService;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        TestApplicationProperties.KeycloakProperties.registerApplicationContextContainerProperties(registry);
        registry.add("application.currency-rate-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successGetCurrencyInfoIntegrationTest() {
        final String AMCM = "AMCM";
        final String requestUrl = String.format("%s/%s", getRateProviderEndpoint, AMCM);

        RateProviderResponse amcm = new RateProviderResponse();
        amcm.setProviderCode(AMCM);
        amcm.setProviderName("Autoridade Monetária de Macau");
        amcm.setDescription("Description");
        amcm.setDate("2026-06-28");
        amcm.setActive(true);

        currencyRateServiceMockServer.stubFor(WireMock.get(requestUrl)
                .willReturn(ResponseDefinitionBuilder.okForJson(amcm)));

        RateProviderResponse rateProviderResponse = currencyRateService.getRateProviderInfo(AMCM).block();

        Assertions.assertDoesNotThrow(() -> currencyRateService.getRateProviderInfo(AMCM));
        Assertions.assertNotNull(rateProviderResponse);
        Assertions.assertEquals(AMCM, rateProviderResponse.getProviderCode());
    }

    @Test
    public void failGetCurrencyInfoWithNullCurrencyIntegrationTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> currencyRateService.getRateProviderInfo(null));
    }

    @Test
    public void failGetCurrencyInfoWithInvalidCurrencyIntegrationTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> currencyRateService.getRateProviderInfo("InvalidCode"));
    }
}
