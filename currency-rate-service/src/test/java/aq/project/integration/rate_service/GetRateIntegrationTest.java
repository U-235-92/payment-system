package aq.project.integration.rate_service;

import aq.project.entity.ConversionRate;
import aq.project.entity.Currency;
import aq.project.entity.RateProvider;
import aq.project.mappers.rate.FrankfurterRateProviderMapper;
import aq.project.repository.ConversionRateRepository;
import aq.project.repository.CurrencyRepository;
import aq.project.repository.RateProviderRepository;
import aq.project.service.RateService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

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
    private FrankfurterRateProviderMapper rateProviderMapper;

    @Autowired
    private RateService rateService;

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
            List<ConversionRate> conversionRateList = rateProviderMapper.getConversionRateList(conversionRatesJsonString, currencyMap);

            currencyRepository.saveAll(currencyMap.values());
            rateProviderRepository.saveAll(rateProviderMap.values());
            conversionRateRepository.saveAll(conversionRateList);

            String from = "RUB";
            String to = "USD";
            String provider = "MNB";
            Assertions.assertDoesNotThrow(() -> rateService.getRate(from, to, provider));
            Assertions.assertNotNull(rateService.getRate(from, to, provider));
        }
    }

    @Test
    public void failGetRateWithNullArgumentsTest() {
        String from = null;
        String to = "";
        String provider = "NBM";
        Assertions.assertThrows(ConstraintViolationException.class, () -> rateService.getRate(from, to, provider));
    }

    @Test
    public void failGetRateWithUnknownCurrencyCodesTest() {
        String from = "ABC";
        String to = "XYZ";
        String provider = "MNB";
        Assertions.assertThrows(IllegalArgumentException.class, () -> rateService.getRate(from, to, provider));
    }
}
