package aq.project.integration.rate_service;

import aq.project.entity.AdjustmentFactor;
import aq.project.entity.ConversionRate;
import aq.project.entity.Currency;
import aq.project.entity.RateProvider;
import aq.project.mappers.rate.FrankfurterRateProviderMapper;
import aq.project.repository.AdjustmentFactorRepository;
import aq.project.repository.ConversionRateRepository;
import aq.project.repository.CurrencyRepository;
import aq.project.repository.RateProviderRepository;
import aq.project.service.RateService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetCurrencyInfoIntegrationTest {

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

    @Test
    public void successCurrencyInfoTest() throws Exception {
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

            String code = "RUB";

            Assertions.assertDoesNotThrow(() -> rateService.getCurrencyInfo(code));
            Assertions.assertNotNull(rateService.getCurrencyInfo(code));
        }
    }

    private Map<String, AdjustmentFactor> getAdjustmentFactors(Map<String, RateProvider> rateProviderMap) {
        final BigDecimal FACTOR = BigDecimal.valueOf(1.085);
        Map<String, AdjustmentFactor> adjustmentFactorMap = new HashMap<>();
        for(String provider : rateProviderMap.keySet()) {
            AdjustmentFactor adjustmentFactor = new AdjustmentFactor();
            adjustmentFactor.setRateProvider(rateProviderMap.get(provider));
            adjustmentFactor.setFactor(FACTOR);
            adjustmentFactor.setCreatedAt(System.currentTimeMillis());
            adjustmentFactor.setModifiedAt(System.currentTimeMillis());
            adjustmentFactorMap.put(provider, adjustmentFactor);
        }
        return adjustmentFactorMap;
    }

    @Test
    public void failGetCurrencyInfoWithNullArgumentsTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> rateService.getCurrencyInfo(null));
    }

    @Test
    public void failGetCurrencyInfoWithUnknownCurrencyCodesTest() {
        String code = "XYZ";
        Assertions.assertThrows(IllegalArgumentException.class, () -> rateService.getCurrencyInfo(code));
    }

    @Test
    public void failGetCurrencyInfoWithInvalidCurrencyCodesTest() {
        String code = "INVALID_CODE";
        Assertions.assertThrows(ConstraintViolationException.class, () -> rateService.getCurrencyInfo(code));
    }
}
