package aq.project.integration.rate_service;

import aq.project.entities.AdjustmentFactor;
import aq.project.entities.ConversionRate;
import aq.project.entities.Currency;
import aq.project.entities.RateProvider;
import aq.project.repositories.AdjustmentFactorRepository;
import aq.project.repositories.ConversionRateRepository;
import aq.project.repositories.CurrencyRepository;
import aq.project.repositories.RateProviderRepository;
import aq.project.services.RateService;
import aq.project.utils.mappers.rate.FrankfurterRateProviderMapper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetRateIntegrationTest {

    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private RateProviderRepository rateProviderRepository;
    @Autowired
    private ConversionRateRepository conversionRateRepository;
    @Autowired
    private AdjustmentFactorRepository adjustmentFactorRepository;

    @Autowired
    private FrankfurterRateProviderMapper rateProviderMapper;

    @Autowired
    private RateService rateService;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = new PostgreSQLContainer("postgres:18-alpine");

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @Test
    public void successGetRateTest() throws Exception {
        try(DataInputStream conversionRateDis = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("src/test/resources/conversion_rates.json")));
            DataInputStream currenciesRateDis = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("src/test/resources/currencies.json")));
            DataInputStream rateProviderDis = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("src/test/resources/rate_providers.json")))
        ) {
            String conversionRatesJsonString = new String(conversionRateDis.readAllBytes());
            String currenciesJsonString = new String(currenciesRateDis.readAllBytes());
            String rateProvidersJsonString = new String(rateProviderDis.readAllBytes());

            Map<String, Currency> currencyMap = rateProviderMapper.getCurrencyMap(currenciesJsonString);
            Map<String, RateProvider> rateProviderMap = rateProviderMapper.getRateProvidersMap(rateProvidersJsonString);
            Map<String, AdjustmentFactor> adjustmentFactorMap = getAdjustmentFactors(rateProviderMap);
            List<ConversionRate> conversionRateList = rateProviderMapper.getConversionRateList(conversionRatesJsonString, currencyMap);

            currencyRepository.saveAll(currencyMap.values());
            rateProviderRepository.saveAll(rateProviderMap.values());
            conversionRateRepository.saveAll(conversionRateList);
            adjustmentFactorRepository.saveAll(adjustmentFactorMap.values());

            String from = "RUB";
            String to = "RUB";
            String provider = null;
            OffsetDateTime date = OffsetDateTime.now();
            Assertions.assertDoesNotThrow(() -> rateService.getRate(from, to, null, date));
            Assertions.assertNotNull(rateService.getRate(from, to, null, date));
        }
    }

    @Test
    public void failGetRateWithNullArgumentsTest() {
        String from = null;
        String to = "";
        String provider = "NBM";
        Assertions.assertThrows(ConstraintViolationException.class, () -> rateService.getRate(from, to, provider, null));
    }

    @Test
    public void failGetRateWithUnknownCurrencyCodesTest() {
        String from = "ABC";
        String to = "XYZ";
        String provider = "MNB";
        Assertions.assertThrows(IllegalStateException.class, () -> rateService.getRate(from, to, provider, null));
    }

    private Map<String, AdjustmentFactor> getAdjustmentFactors(Map<String, RateProvider> rateProviderMap) {
        final BigDecimal FACTOR = BigDecimal.valueOf(1.085);
        Map<String, AdjustmentFactor> adjustmentFactorMap = new HashMap<>();
        for(String provider : rateProviderMap.keySet()) {
            AdjustmentFactor adjustmentFactor = new AdjustmentFactor();
            adjustmentFactor.setRateProvider(rateProviderMap.get(provider));
            adjustmentFactor.setFactor(FACTOR);
            adjustmentFactor.setCreatedAt(OffsetDateTime.now());
            adjustmentFactor.setModifiedAt(OffsetDateTime.now());
            adjustmentFactorMap.put(provider, adjustmentFactor);
        }
        return adjustmentFactorMap;
    }
}
