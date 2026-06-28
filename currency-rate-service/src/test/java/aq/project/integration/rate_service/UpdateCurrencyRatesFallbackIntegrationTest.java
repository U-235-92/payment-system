package aq.project.integration.rate_service;

import aq.project.service.RateService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@WireMockTest
@ActiveProfiles("rate-service-fallback-test")
@EnableWireMock(value = @ConfigureWireMock(name = "rate-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateCurrencyRatesFallbackIntegrationTest {

    @InjectWireMock("rate-service-mock")
    private WireMockServer rateServiceWireMockServer;

    @Autowired
    private RateService rateService;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        registry.add("application.client.frankfurter.base-url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    void checkLogsFallbackUpdateCurrencyRatesTest() throws Exception {
//        An empty call on not exist rate service URL just for check fallback logic
//        To check fallback see logs in console after test was finished
        rateService.updateCurrencyRates();
    }
}
