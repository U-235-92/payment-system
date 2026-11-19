package aq.project.services;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ConversionRateService {

    private final CurrencyRateService currencyRateService;

    Mono<BigDecimal> getConversionRate(
            String requestCurrencyCode,
            String walletCurrencyCode
    ) {
        if(isInvalidCurrencyCode(requestCurrencyCode)) {
            String msg = String.format(
                    "Error occurred during getting conversion rate while deposit transaction was creating: " +
                            "received invalid transaction request currency code: [%s]", requestCurrencyCode);

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(isInvalidCurrencyCode(walletCurrencyCode)) {
            String msg = String.format(
                    "Error occurred during getting conversion rate while deposit transaction was creating: " +
                            "received invalid wallet currency code: [%s]", walletCurrencyCode);

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        return currencyRateService
                .getRate(requestCurrencyCode, walletCurrencyCode, null, null)
                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getRate())));

    }

    boolean isInvalidCurrencyCode(
            String currencyCode
    ) {
        return currencyCode == null || !Pattern.compile("^[A-Z]{3}$")
                .matcher(currencyCode)
                .matches();
    }

    boolean isValidConversionRate(
            BigDecimal conversionRate
    ) {
        return conversionRate != null && conversionRate.compareTo(BigDecimal.ZERO) > 0;
    }
}