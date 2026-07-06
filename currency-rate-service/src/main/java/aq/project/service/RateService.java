package aq.project.service;

import aq.project.client.FrankfurterRateProviderClient;
import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import aq.project.entity.AdjustmentFactor;
import aq.project.entity.ConversionRate;
import aq.project.entity.Currency;
import aq.project.entity.RateProvider;
import aq.project.mappers.dto.CurrencyResponseMapper;
import aq.project.mappers.dto.RateProviderResponseMapper;
import aq.project.mappers.dto.RateResponseMapper;
import aq.project.mappers.rate.AbstractRateProviderMapper;
import aq.project.repository.AdjustmentFactorRepository;
import aq.project.repository.ConversionRateRepository;
import aq.project.repository.CurrencyRepository;
import aq.project.repository.RateProviderRepository;
import aq.project.util.telemetry.TraceContext;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static aq.project.util.constants.CustomConstants.EQUAL_CURRENCY_RATE;
import static aq.project.util.constants.CustomConstants.ISO_DATE_FORMAT;

@Slf4j
@Service
public class RateService {

    @Value("${spring.application.name}")
    private String tracerName;

    @Value("${application.rates.provider.default.code}")
    private String defaultRateProviderCode;

    private final CurrencyRepository currencyRepository;
    private final RateProviderRepository rateProviderRepository;
    private final ConversionRateRepository conversionRateRepository;
    private final AdjustmentFactorRepository adjustmentFactorRepository;

    private final FrankfurterRateProviderClient frankfurterRateProviderClient;

    private final AbstractRateProviderMapper rateProviderMapper;

    private final OpenTelemetry openTelemetry;

    private final TraceContext traceContext;

    private final RateResponseMapper rateResponseMapper;
    private final CurrencyResponseMapper currencyResponseMapper;
    private final RateProviderResponseMapper rateProviderResponseMapper;

    public RateService(
            CurrencyRepository currencyRepository,
            RateProviderRepository rateProviderRepository,
            ConversionRateRepository conversionRateRepository,
            AdjustmentFactorRepository adjustmentFactorRepository,
            FrankfurterRateProviderClient frankfurterRateProviderClient,
            @Qualifier("frankfurter") AbstractRateProviderMapper rateProviderMapper,
            OpenTelemetry openTelemetry,
            TraceContext traceContext,
            RateResponseMapper rateResponseMapper,
            CurrencyResponseMapper currencyResponseMapper,
            RateProviderResponseMapper rateProviderResponseMapper) {
        this.currencyRepository = currencyRepository;
        this.rateProviderRepository = rateProviderRepository;
        this.conversionRateRepository = conversionRateRepository;
        this.adjustmentFactorRepository = adjustmentFactorRepository;
        this.frankfurterRateProviderClient = frankfurterRateProviderClient;
        this.rateProviderMapper = rateProviderMapper;
        this.openTelemetry = openTelemetry;
        this.traceContext = traceContext;
        this.rateResponseMapper = rateResponseMapper;
        this.currencyResponseMapper = currencyResponseMapper;
        this.rateProviderResponseMapper = rateProviderResponseMapper;
    }

    public RateResponse getRate(String from, String to, String provider, LocalDate date) {
        RateResponse rateResponse;
        if(from.equals(to)) {
            rateResponse = new RateResponse();
            rateResponse.setSourceCode(from);
            rateResponse.setDestinationCode(to);
            rateResponse.setProviderCode(provider);
            String rateDate = (date == null)
                ? DateTimeFormatter.ofPattern(ISO_DATE_FORMAT).format(LocalDate.now())
                : DateTimeFormatter.ofPattern(ISO_DATE_FORMAT).format(date);
            rateResponse.setRateDate(rateDate);
            rateResponse.setRate(BigDecimal.valueOf(EQUAL_CURRENCY_RATE));
        } else {
            LocalDate rateDate = (date == null)
                    ? LocalDate.now()
                    : date;
            Optional<ConversionRate> conversionRateOptional = conversionRateRepository
                    .findConversionRateBySourceCurrencyAndDestinationCurrency(from, to, rateDate);
            ConversionRate conversionRate = conversionRateOptional.orElseThrow(
                    () -> new IllegalArgumentException(String.format("Conversion rate not found for: [%s -> %s] on date: %s",
                            from, to, DateTimeFormatter.ofPattern(ISO_DATE_FORMAT).format(rateDate)))
            );
            rateResponse = rateResponseMapper.toRateResponse(conversionRate, provider);
        }
        AdjustmentFactor adjustmentFactor = (provider == null)
                ? adjustmentFactorRepository.findByRateProviderCode(defaultRateProviderCode)
                    .orElseThrow(() -> new IllegalStateException(
                        "No default adjustment factor found for case when no provider was assigned"))
                : adjustmentFactorRepository.findByRateProviderCode(provider)
                    .orElseThrow(() -> new IllegalArgumentException(String
                        .format("No adjustment factor found for rate provider with code: %s", provider)));
        rateResponse.setRate(calculateAdjustedRate(adjustmentFactor, rateResponse));
        return rateResponse;
    }

    private BigDecimal calculateAdjustedRate(AdjustmentFactor adjustmentFactor, RateResponse rateResponse) {
        BigDecimal factor = adjustmentFactor.getFactor();
        BigDecimal rate = rateResponse.getRate();
        return rate.multiply(factor);
    }

    public List<CurrencyResponse> getCurrencies() {
        List<CurrencyResponse> currencyResponseList = new ArrayList<>();
        currencyRepository.findAll()
                .forEach(currency ->
                        currencyResponseList.add(currencyResponseMapper.toCurrencyResponse(currency)));
        return currencyResponseList;
    }

    public List<RateProviderResponse> getRateProviders() {
        List<RateProviderResponse> rateProviderResponseList = new ArrayList<>();
        rateProviderRepository.findAll()
                .forEach(rateProvider ->
                        rateProviderResponseList.add(rateProviderResponseMapper.toRateProviderResponse(rateProvider)));
        return rateProviderResponseList;
    }

    public CurrencyResponse getCurrencyInfo(String code) {
        Currency currency = currencyRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Incorrect currency code: %s", code)));
        return currencyResponseMapper.toCurrencyResponse(currency);
    }

    public RateProviderResponse getRateProviderInfo(String code) {
        RateProvider rateProvider = rateProviderRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Incorrect rate provider code: %s", code)));
        return rateProviderResponseMapper.toRateProviderResponse(rateProvider);
    }

    @Transactional
    @Retry(name = "update-currency-rates", fallbackMethod = "updateCurrencyRatesFallback")
    @CircuitBreaker(name = "update-currency-rates", fallbackMethod = "updateCurrencyRatesFallback")
    @Bulkhead(name = "update-currency-rates", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "updateCurrencyRatesFallback")
    @Scheduled(
            fixedRateString = "${application.scheduler.update-rate-scheduler.fixed-rate}",
            timeUnit =  TimeUnit.SECONDS
    )
    @SchedulerLock(
            name = "${application.schedlock.update-rate-shedlock.name}",
            lockAtLeastFor = "${application.schedlock.update-rate-shedlock.lock-at-least-for}",
            lockAtMostFor = "${application.schedlock.update-rate-shedlock.lock-at-most-for}"
    )
    public void updateCurrencyRates() throws Exception {
        Map<String, RateProvider> rateProviderMap = rateProviderMapper.getRateProvidersMap(frankfurterRateProviderClient.getProviders());
        Map<String, Currency> currencyMap = rateProviderMapper.getCurrencyMap(frankfurterRateProviderClient.getCurrencies());
        List<List<ConversionRate>> conversionRatesList = getConversionRatesForCurrency(currencyMap);
        rateProviderRepository.saveAll(rateProviderMap.values());
        currencyRepository.saveAll(currencyMap.values());
        conversionRatesList.forEach(conversionRateRepository::saveAll);
    }

    private List<List<ConversionRate>> getConversionRatesForCurrency(Map<String, Currency> currencyMap) throws Exception {
        List<List<ConversionRate>> conversionRatesList = new ArrayList<>();
        for(String currencyCode : currencyMap.keySet()) {
            String ratesJsonResponse = frankfurterRateProviderClient.getRates(currencyCode, "providers");
            conversionRatesList.add(rateProviderMapper.getConversionRateList(ratesJsonResponse, currencyMap));
        }
        return conversionRatesList;
    }

    private void updateCurrencyRatesFallback(Exception e) {
        final String service = "currency-rate-service";
        final String action = "update-currency-rates-fallback";
        Span span = openTelemetry.getTracer(tracerName).spanBuilder(action).startSpan();
        String traceId = traceContext.getTraceId();
        String spanId = span.getSpanContext().getSpanId();
        try(Scope scope = span.makeCurrent()) {
            String msg = String.format("[%s-%s][%s -> %s]: %s", traceId, spanId, service, action, e.getMessage());
            log.error(msg, e);
        } finally {
            span.end();
            traceContext.clean();
        }
    }
}
