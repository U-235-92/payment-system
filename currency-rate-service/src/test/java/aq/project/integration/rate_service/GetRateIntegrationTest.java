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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DirtiesContext
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
            LocalDate date = LocalDate.of(2026, 6, 21);
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
        Assertions.assertThrows(IllegalArgumentException.class, () -> rateService.getRate(from, to, provider, null));
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

//    private Map<String, AdjustmentFactor> getAdjustmentFactors(String rateProvidersJsonString) throws JsonProcessingException {
//        final BigDecimal FACTOR = BigDecimal.valueOf(1.085);
//        Map<String, AdjustmentFactor> adjustmentFactorMap = new HashMap<>();
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode root = objectMapper.readTree(rateProvidersJsonString);
//        root.forEach(node -> {
//            String rateProviderCode = node.get("key").asText();
//            String rateProviderName = node.get("name").asText();
//            String rateProviderDescription = node.get("data_url").asText();
//
//            RateProvider rateProvider = new RateProvider();
//            rateProvider.setCode(rateProviderCode);
//            rateProvider.setName(rateProviderName);
//            rateProvider.setActive(true);
//            rateProvider.setDescription(rateProviderDescription);
//            rateProvider.setCreatedAt(LocalDate.now());
//            rateProvider.setModifiedAt(LocalDate.now());
//
//            AdjustmentFactor adjustmentFactor = new AdjustmentFactor();
//            adjustmentFactor.setRateProvider(rateProvider);
//            adjustmentFactor.setFactor(FACTOR);
//            adjustmentFactor.setCreatedAt(System.currentTimeMillis());
//            adjustmentFactor.setModifiedAt(System.currentTimeMillis());
//            adjustmentFactorMap.put(rateProviderCode, adjustmentFactor);
//        });
//        return adjustmentFactorMap;
//    }
}
