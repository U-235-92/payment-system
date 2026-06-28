package aq.project.mappers.rate;

import aq.project.entity.ConversionRate;
import aq.project.entity.Currency;
import aq.project.entity.RateProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aq.project.mappers.rate.FrankfurterRateProviderMapper.FrankfurterRateProviderProperties.*;

@Slf4j
@RequiredArgsConstructor
@Component(value = "frankfurter")
public class FrankfurterRateProviderMapper implements AbstractRateProviderMapper {

    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT);

    private final ObjectMapper objectMapper;

    @Override
    public Map<String, RateProvider> getRateProvidersMap(String rateClientResponse) throws Exception {
        Map<String, RateProvider> rateProviders = new HashMap<>();
        JsonNode root = objectMapper.readTree(rateClientResponse);
        root.forEach(node -> {
            RateProvider rateProvider = getRateProvider(node);
            rateProviders.put(rateProvider.getCode(), rateProvider);
        });
        return rateProviders;
    }

    private RateProvider getRateProvider(JsonNode node) {
        if(isIllegalRateProviderJsonNode(node))
            throw new IllegalArgumentException("Illegal rate provider JSON node");
        String providerCode = node.get(KEY).asText();
        LocalDate createdAt = getJsonNodeLocalDateFieldValue(node, START_DATE);
        LocalDate modifiedAt = getJsonNodeLocalDateFieldValue(node, END_DATE);
        String name = node.get(NAME).asText();
        String description = (node.get(DATA_URL) != null) ? node.get(DATA_URL).asText() : null;
        return new RateProvider(providerCode, createdAt, modifiedAt, name, description, true);
    }

    private boolean isIllegalRateProviderJsonNode(JsonNode node) {
        return node.get(KEY) == null ||
                node.get(START_DATE) == null ||
                node.get(END_DATE) == null ||
                node.get(NAME) == null ||
                node.get(DATA_URL) == null;
    }

    @Override
    public Map<String, Currency> getCurrencyMap(String rateClientResponse) throws Exception {
        Map<String, Currency> currencyMap = new HashMap<>();
        JsonNode root = objectMapper.readTree(rateClientResponse);
        root.forEach(node -> {
            Currency currency = getCurrency(node);
            currencyMap.put(currency.getIsoCode(), currency);
        });
        return currencyMap;
    }

    private Currency getCurrency(JsonNode node) {
        if(isIllegalCurrencyJsonNode(node))
            throw new IllegalArgumentException("Illegal currency JSON node");
        String isoCode = node.get(ISO_CODE).asText();
        int isoNumeric = node.get(ISO_NUMERIC).asInt();
        LocalDate createdAt = getJsonNodeLocalDateFieldValue(node, START_DATE);
        LocalDate modifiedAt = getJsonNodeLocalDateFieldValue(node, END_DATE);
        String description = node.get(NAME).asText();
        String symbol = node.get(SYMBOL).asText();
        return new Currency(isoCode, isoNumeric, createdAt, modifiedAt, description, true, symbol);
    }

    private boolean isIllegalCurrencyJsonNode(JsonNode node) {
        return node.get(ISO_CODE) == null ||
                node.get(ISO_NUMERIC) == null ||
                node.get(START_DATE) == null ||
                node.get(END_DATE) == null ||
                node.get(NAME) == null ||
                node.get(SYMBOL) == null;
    }

    @Override
    public List<ConversionRate> getConversionRateList(String rateClientResponse, Map<String, Currency> currenciesMap) throws Exception {
        List<ConversionRate> conversionRates = new ArrayList<>();
        JsonNode root = objectMapper.readTree(rateClientResponse);
        root.forEach(node ->  conversionRates.add(getConversionRate(node, currenciesMap)));
        return conversionRates;
    }

    private ConversionRate getConversionRate(JsonNode node, Map<String, Currency> currenciesMap) {
        if(currenciesMap == null || currenciesMap.isEmpty())
            throw new IllegalArgumentException("Currencies map is null or empty");
        if(isIllegalConversionRateJsonNode(node))
            throw new IllegalArgumentException("Illegal conversion rate JSON node");
        String sourceCurrencyCode = node.get(BASE).asText();
        String destinationCurrencyCode = node.get(QUOTE).asText();
        Currency sourceCurrency = currenciesMap.computeIfPresent(sourceCurrencyCode, (k, v) -> v);
        Currency destinationCurrency = currenciesMap.computeIfPresent(destinationCurrencyCode, (k, v) -> v);
        LocalDate rateDate = getJsonNodeLocalDateFieldValue(node, DATE);
        double rate = node.get(RATE).asDouble();
        JsonNode providersArrayNode = node.get(PROVIDERS);
        Map<String, Double> providerRateMap = new HashMap<>();
        if(providersArrayNode != null) {
            providersArrayNode.forEach(providerNode -> {
                if(isIllegalProviderJsonNode(providerNode))
                    throw new IllegalArgumentException("Illegal provider JSON node");
                providerRateMap.put(providerNode.get(KEY).asText(), providerNode.get(RATE).asDouble());
            });
        }
        return new ConversionRate(sourceCurrency, destinationCurrency, rateDate, rate, providerRateMap);
    }

    private boolean isIllegalConversionRateJsonNode(JsonNode node) {
        return node.get(BASE) == null || node.get(QUOTE) == null || node.get(DATE) == null || node.get(RATE) == null;
    }

    private boolean isIllegalProviderJsonNode(JsonNode node) {
        return node.get(KEY) == null || node.get(RATE) == null;
    }

    private LocalDate getJsonNodeLocalDateFieldValue(JsonNode node, String fieldName) {
        return LocalDate.from(DATE_TIME_FORMATTER.parse(node.get(fieldName).asText()));
    }

    interface FrankfurterRateProviderProperties {

        String START_DATE = "start_date";
        String END_DATE = "end_date";
        String KEY = "key";
        String NAME = "name";
        String ISO_NUMERIC = "iso_numeric";
        String ISO_CODE = "iso_code";
        String SYMBOL = "symbol";
        String DATE =  "date";
        String BASE =  "base";
        String QUOTE = "quote";
        String PROVIDERS = "providers";
        String RATE = "rate";
        String DATA_URL = "data_url";
    }
}
