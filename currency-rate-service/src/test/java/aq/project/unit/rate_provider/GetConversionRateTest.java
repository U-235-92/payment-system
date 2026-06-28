package aq.project.unit.rate_provider;

import aq.project.entity.ConversionRate;
import aq.project.entity.Currency;
import aq.project.mappers.rate.FrankfurterRateProviderMapper;
import aq.project.util.telemetry.TraceContext;
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
                            new FileInputStream("src/test/resources/currencies.json")))
        ) {
            String conversionRatesJsonString = new String(conversionRateDis.readAllBytes());
            String currenciesJsonString = new String(currenciesRateDis.readAllBytes());
            Map<String, Currency> currencyMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getCurrencyMap(currenciesJsonString);
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
                            new FileInputStream("src/test/resources/currencies.json")))
        ) {
            String currenciesJsonString = new String(currenciesRateDis.readAllBytes());
            Map<String, Currency> currencyMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getCurrencyMap(currenciesJsonString);
            Assertions.assertThrows(JsonParseException.class, () ->
                    FRANKFURTER_RATE_PROVIDER_MAPPER.getConversionRateList("FooBar", currencyMap));
        }
    }

    @Test
    public void failGetConversionRateListWithAnotherJsonStringUnitTest() throws Exception {
        try(DataInputStream currenciesRateDis = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream("src/test/resources/currencies.json")))
        ) {
            String anotherJsonString = new String(currenciesRateDis.readAllBytes());
            Map<String, Currency> currencyMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getCurrencyMap(anotherJsonString);
            Assertions.assertThrows(IllegalArgumentException.class, () ->
                    FRANKFURTER_RATE_PROVIDER_MAPPER.getConversionRateList(anotherJsonString, currencyMap));
        }
    }
}
