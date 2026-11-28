package aq.project.services;

import aq.project.clients.FrankfurterRateProviderClient;
import aq.project.dto.CurrencyResponseDto;
import aq.project.dto.RateProviderResponseDto;
import aq.project.dto.RateResponseDto;
import aq.project.entities.AdjustmentFactor;
import aq.project.entities.ConversionRate;
import aq.project.entities.Currency;
import aq.project.entities.RateProvider;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.AdjustmentFactorRepository;
import aq.project.repositories.ConversionRateRepository;
import aq.project.repositories.CurrencyRepository;
import aq.project.repositories.RateProviderRepository;
import aq.project.utils.mappers.dto.CurrencyResponseMapper;
import aq.project.utils.mappers.dto.RateProviderResponseMapper;
import aq.project.utils.mappers.dto.RateResponseMapper;
import aq.project.utils.mappers.rate.AbstractRateProviderMapper;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static aq.project.utils.constants.CustomConstants.EQUAL_CURRENCY_RATE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

@Slf4j
@Service
public class RateService {

    @Value("${application.rates.provider.default.code}")
    private String defaultRateProviderCode;

    private final CurrencyRepository currencyRepository;
    private final RateProviderRepository rateProviderRepository;
    private final ConversionRateRepository conversionRateRepository;
    private final AdjustmentFactorRepository adjustmentFactorRepository;

    private final FrankfurterRateProviderClient frankfurterRateProviderClient;

    private final AbstractRateProviderMapper rateProviderMapper;

    private final RateResponseMapper rateResponseMapper;
    private final CurrencyResponseMapper currencyResponseMapper;
    private final RateProviderResponseMapper rateProviderResponseMapper;

    private final Fallback fallback;

    public RateService(
            CurrencyRepository currencyRepository,
            RateProviderRepository rateProviderRepository,
            ConversionRateRepository conversionRateRepository,
            AdjustmentFactorRepository adjustmentFactorRepository,
            FrankfurterRateProviderClient frankfurterRateProviderClient,
            @Qualifier("frankfurter") AbstractRateProviderMapper rateProviderMapper,
            RateResponseMapper rateResponseMapper,
            CurrencyResponseMapper currencyResponseMapper,
            RateProviderResponseMapper rateProviderResponseMapper,
            Fallback fallback) {
        this.currencyRepository = currencyRepository;
        this.rateProviderRepository = rateProviderRepository;
        this.conversionRateRepository = conversionRateRepository;
        this.adjustmentFactorRepository = adjustmentFactorRepository;
        this.frankfurterRateProviderClient = frankfurterRateProviderClient;
        this.rateProviderMapper = rateProviderMapper;
        this.rateResponseMapper = rateResponseMapper;
        this.currencyResponseMapper = currencyResponseMapper;
        this.rateProviderResponseMapper = rateProviderResponseMapper;
        this.fallback = fallback;
    }

    @RateLimiter(
            name = "get-rate",
            fallbackMethod = "getRateRateLimiterFallback"
    )
    public RateResponseDto getRate(
            String from,
            String to,
            String provider,
            OffsetDateTime date
    ) {
        if(from.equals(to)) {
            RateResponseDto rateResponse = new RateResponseDto();
            rateResponse.setSourceCode(from);
            rateResponse.setDestinationCode(to);
            rateResponse.setProviderCode(provider);
            rateResponse.setRateDate(date);
            rateResponse.setRate(BigDecimal.valueOf(EQUAL_CURRENCY_RATE));

            return rateResponse;

        } else {
            if(date == null) {
                Optional<ConversionRate> conversionRateOptional = conversionRateRepository
                        .findConversionRate(from, to);

                ConversionRate conversionRate = conversionRateOptional
                        .orElseThrow(() -> new EntityNotFoundException(
                                String.format("Conversion rate not found for: [%s -> %s]", from, to)));

                return getRateResponseDto(conversionRate, provider);

            } else {
                Optional<ConversionRate> conversionRateOptional = conversionRateRepository
                        .findConversionRate(from, to, date);

                ConversionRate conversionRate = conversionRateOptional
                        .orElseThrow(() -> new EntityNotFoundException(
                                String.format("Conversion rate not found for: [%s -> %s] on date: %s",
                                from, to, ISO_OFFSET_DATE_TIME.format(date))));

                return getRateResponseDto(conversionRate, provider);
            }
        }
    }

    private RateResponseDto getRateRateLimiterFallback(
            String from,
            String to,
            String provider,
            OffsetDateTime date,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-rate";
        String message = String.format(
                "Exception occurred during getting currency rate from: [%s] to: [%s] " +
                "for provider: [%s]. Exception message: %s",
                from, to, provider, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    private RateResponseDto getRateResponseDto(
            ConversionRate conversionRate,
            String provider
    ) {
        AdjustmentFactor adjustmentFactor = getAdjustmentFactor(provider);

        RateResponseDto rateResponse = rateResponseMapper.toRateResponseDto(conversionRate, provider);
        rateResponse.setRate(calculateAdjustedRate(adjustmentFactor, rateResponse));

        return rateResponse;
    }

    private AdjustmentFactor getAdjustmentFactor(
            String provider
    ) {
        return (provider == null)
                ? adjustmentFactorRepository.findByRateProviderCode(defaultRateProviderCode)
                    .orElseThrow(() -> new IllegalStateException(
                        "No default adjustment factor found for case when no provider was assigned"))
                : adjustmentFactorRepository.findByRateProviderCode(provider)
                    .orElseThrow(() -> new IllegalArgumentException(String
                        .format("No adjustment factor found for rate provider with code: %s", provider)));
    }

    private BigDecimal calculateAdjustedRate(
            AdjustmentFactor adjustmentFactor,
            RateResponseDto rateResponse
    ) {
        BigDecimal factor = adjustmentFactor.getFactor();
        BigDecimal rate = rateResponse.getRate();

        return rate.multiply(factor);
    }

    public List<CurrencyResponseDto> getCurrencies() {
        List<CurrencyResponseDto> currencyResponseList = new ArrayList<>();
        currencyRepository.findAll()
                .forEach(currency -> currencyResponseList
                        .add(currencyResponseMapper.toCurrencyResponseDto(currency)));

        return currencyResponseList;
    }

    public List<RateProviderResponseDto> getRateProviders() {
        List<RateProviderResponseDto> rateProviderResponseList = new ArrayList<>();
        rateProviderRepository.findAll()
                .forEach(rateProvider -> rateProviderResponseList
                        .add(rateProviderResponseMapper.toRateProviderResponseDto(rateProvider)));

        return rateProviderResponseList;
    }

    @RateLimiter(
            name = "get-currency-info",
            fallbackMethod = "getCurrencyInfoRateLimiterFallback"
    )
    public CurrencyResponseDto getCurrencyInfo(
            String code
    ) {
        Currency currency = currencyRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Not found currency info for currency code: [%s]", code)));

        return currencyResponseMapper.toCurrencyResponseDto(currency);
    }

    private CurrencyResponseDto getCurrencyInfoRateLimiterFallback(
            String code,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-currency-info";
        String message = String.format(
                "Exception occurred during getting currency info for code: [%s]. Exception message: %s",
                code, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "get-rate-provider-info",
            fallbackMethod = "getRateProviderInfoRateLimiterFallback"
    )
    public RateProviderResponseDto getRateProviderInfo(
            String code
    ) {
        RateProvider rateProvider = rateProviderRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Not found rate provider info for rate provider code: [%s]", code)));

        return rateProviderResponseMapper.toRateProviderResponseDto(rateProvider);
    }

    private RateProviderResponseDto getRateProviderInfoRateLimiterFallback(
            String code,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-rate-provider-info";
        String message = String.format(
                "Exception occurred during getting rate provider info for code: [%s]. Exception message: %s",
                code, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @Retry(
            name = "update-currency-rates",
            fallbackMethod = "updateCurrencyRatesRetryFallback"
    )
    @Bulkhead(
            name = "update-currency-rates",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "updateCurrencyRatesBulkheadFallback"
    )
    @RateLimiter(
            name = "update-currency-rates",
            fallbackMethod = "updateCurrencyRatesRateLimiterFallback"
    )
    @CircuitBreaker(
            name = "update-currency-rates",
            fallbackMethod = "updateCurrencyRatesCircuitBreakerFallback"
    )
    @Scheduled(
            fixedRateString = "${application.scheduler.update-rate-scheduler.fixed-rate}",
            timeUnit =  TimeUnit.SECONDS
    )
    @SchedulerLock(
            name = "${application.schedlock.update-rate-shedlock.name}",
            lockAtLeastFor = "${application.schedlock.update-rate-shedlock.lock-at-least-for}",
            lockAtMostFor = "${application.schedlock.update-rate-shedlock.lock-at-most-for}"
    )
    @Transactional
    public void updateCurrencyRates() throws Exception {
        Map<String, RateProvider> rateProviderMap = rateProviderMapper
                .getRateProvidersMap(frankfurterRateProviderClient.getProviders());

        Map<String, Currency> currencyMap = rateProviderMapper
                .getCurrencyMap(frankfurterRateProviderClient.getCurrencies());

        List<List<ConversionRate>> conversionRatesList = getConversionRatesForCurrency(currencyMap);

        rateProviderRepository.saveAll(rateProviderMap.values());
        currencyRepository.saveAll(currencyMap.values());

        conversionRatesList.forEach(conversionRateRepository::saveAll);
    }

    private void updateCurrencyRatesRetryFallback(
            Exception exception
    ) {
        String action = "update-currency-rates";
        String message = String.format(
                "Exception occurred during update currency rates. Exception message: %s", exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);
    }

    private void updateCurrencyRatesBulkheadFallback(
            Exception exception
    ) {
        String action = "update-currency-rates";
        String message = String.format(
                "Exception occurred during update currency rates. Exception message: %s", exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);
    }

    private void updateCurrencyRatesRateLimiterFallback(
            Exception exception
    ) {
        String action = "update-currency-rates";
        String message = String.format(
                "Exception occurred during update currency rates. Exception message: %s", exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);
    }

    private void updateCurrencyRatesCircuitBreakerFallback(
            Exception exception
    ) {
        String action = "update-currency-rates";
        String message = String.format(
                "Exception occurred during update currency rates. Exception message: %s", exception.getMessage());

        fallback.handleCircuitBreakerFallback(action, message, exception);
    }

    private List<List<ConversionRate>> getConversionRatesForCurrency(
            Map<String, Currency> currencyMap
    ) throws Exception {
        List<List<ConversionRate>> conversionRatesList = new ArrayList<>();

        for(String currencyCode : currencyMap.keySet()) {
            String ratesJsonResponse = frankfurterRateProviderClient.getRates(currencyCode, "providers");
            conversionRatesList.add(rateProviderMapper.getConversionRateList(ratesJsonResponse, currencyMap));
        }
        return conversionRatesList;
    }
}
