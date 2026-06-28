package aq.project.unit.rate_provider;

import aq.project.entity.RateProvider;
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
import java.util.Map;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class GetRateProvidersTest {

    private static FrankfurterRateProviderMapper FRANKFURTER_RATE_PROVIDER_MAPPER;

    @BeforeAll
    public static void setup() {
        TraceContext traceContext = new TraceContext();
        ObjectMapper objectMapper = new ObjectMapper();
        OpenTelemetry openTelemetry = OpenTelemetry.noop();
        FRANKFURTER_RATE_PROVIDER_MAPPER = new FrankfurterRateProviderMapper(objectMapper);
    }

    @Test
    public void successGetRateProvidersMapUnitTest() throws Exception {
        try(DataInputStream dis = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("src/test/resources/rate_providers.json")))) {
            String rateProvidersJsonString = new String(dis.readAllBytes());
            Assertions.assertDoesNotThrow(() ->
                    FRANKFURTER_RATE_PROVIDER_MAPPER.getRateProvidersMap(rateProvidersJsonString));
            Map<String, RateProvider> rateProviderMap = FRANKFURTER_RATE_PROVIDER_MAPPER.getRateProvidersMap(rateProvidersJsonString);
            Assertions.assertNotNull(rateProviderMap);
            Assertions.assertNotNull(rateProviderMap.get("AMCM"));
            Assertions.assertEquals("AMCM", rateProviderMap.get("AMCM").getCode());
        }
    }

    @Test
    public void failGetRateProvidersMapWithNullRateProvidersJsonStringUnitTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                FRANKFURTER_RATE_PROVIDER_MAPPER.getRateProvidersMap(null));
    }

    @Test
    public void failGetRateProvidersMapWithWrongRateProvidersJsonStringUnitTest() {
        Assertions.assertThrows(JsonParseException.class, () ->
                FRANKFURTER_RATE_PROVIDER_MAPPER.getRateProvidersMap("FooBar"));
    }

    @Test
    public void failGetRateProvidersMapWithAnotherJsonStringUnitTest() throws Exception {
        try(DataInputStream dis = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("src/test/resources/currencies.json")))) {
            String anotherJsonString = new String(dis.readAllBytes());
            Assertions.assertThrows(IllegalArgumentException.class, () ->
                    FRANKFURTER_RATE_PROVIDER_MAPPER.getRateProvidersMap(anotherJsonString));
        }
    }
}
