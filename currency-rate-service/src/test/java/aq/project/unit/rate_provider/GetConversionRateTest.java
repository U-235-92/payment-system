package aq.project.unit.rate_provider;

import aq.project.entities.AdjustmentFactor;
import aq.project.entities.ConversionRate;
import aq.project.entities.Currency;
import aq.project.entities.RateProvider;
import aq.project.utils.mappers.rate.FrankfurterRateProviderMapper;
import aq.project.utils.telemetry.TraceContext;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class GetConversionRateTest {

    private static FrankfurterRateProviderMapper FRANKFURTER_RATE_PROVIDER_MAPPER;

    @BeforeAll
    public static void setup() {
        TraceContext traceContext = new TraceContext();
        ObjectMapper objectMapper = new ObjectMapper();
        OpenTelemetry openTelemetry = OpenTelemetry.noop();
        FRANKFURTER_RATE_PROVIDER_MAPPER = new FrankfurterRateProviderMapper(objectMapper);
    }

    @Test
    public void successGetConversionRateListUnitTest() throws Exception {
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
            String rateProvidersJsonString = new String(rateProviderDis.readAllBytes());
            String conversionRatesJsonString = new String(conversionRateDis.readAllBytes());
            String currenciesJsonString = new String(currenciesRateDis.readAllBytes());
            Map<String, RateProvider> rateProviderMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getRateProvidersMap(rateProvidersJsonString);
            Map<String, Currency> currencyMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getCurrencyMap(currenciesJsonString);
            Map<String, AdjustmentFactor> adjustmentFactorMap = getAdjustmentFactors(rateProviderMap);
            Assertions.assertDoesNotThrow(() ->
                    FRANKFURTER_RATE_PROVIDER_MAPPER.getConversionRateList(conversionRatesJsonString, currencyMap));
            List<ConversionRate> conversionRateList = FRANKFURTER_RATE_PROVIDER_MAPPER
                    .getConversionRateList(conversionRatesJsonString, currencyMap);
            Assertions.assertNotNull(conversionRateList);
        }
    }

    @Test
    public void failGetConversionRateListWithNullConversionRateJsonStringUnitTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                FRANKFURTER_RATE_PROVIDER_MAPPER.getConversionRateList(null, null));
    }

    @Test
    public void failGeConversionRateListWithWrongConversionRateJsonStringUnitTest() throws Exception {
        try(DataInputStream currenciesRateDis = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("src/test/resources/currencies.json")));
            DataInputStream rateProviderDis = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("src/test/resources/rate_providers.json")))
        ) {
            String rateProvidersJsonString = new String(rateProviderDis.readAllBytes());
            String currenciesJsonString = new String(currenciesRateDis.readAllBytes());
            Map<String, RateProvider> rateProviderMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getRateProvidersMap(rateProvidersJsonString);
            Map<String, AdjustmentFactor> adjustmentFactorMap = getAdjustmentFactors(rateProviderMap);
            Map<String, Currency> currencyMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getCurrencyMap(currenciesJsonString);
            Assertions.assertThrows(JsonParseException.class, () ->
                    FRANKFURTER_RATE_PROVIDER_MAPPER.getConversionRateList("FooBar", currencyMap));
        }
    }

    @Test
    public void failGetConversionRateListWithAnotherJsonStringUnitTest() throws Exception {
        try(DataInputStream currenciesRateDis = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("src/test/resources/currencies.json")));
            DataInputStream rateProviderDis = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("src/test/resources/rate_providers.json")))
        ) {
            String rateProvidersJsonString = new String(rateProviderDis.readAllBytes());
            String anotherJsonString = new String(currenciesRateDis.readAllBytes());
            Map<String, RateProvider> rateProviderMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getRateProvidersMap(rateProvidersJsonString);
            Map<String, AdjustmentFactor> adjustmentFactorMap = getAdjustmentFactors(rateProviderMap);
            Map<String, Currency> currencyMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getCurrencyMap(anotherJsonString);
            Assertions.assertThrows(IllegalArgumentException.class, () ->
                    FRANKFURTER_RATE_PROVIDER_MAPPER.getConversionRateList(anotherJsonString, currencyMap));
        }
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
