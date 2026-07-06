package aq.project.services;

import aq.project.clients.RateClient;
import aq.project.dto.CurrencyResponse;
import aq.project.dto.ErrorDTO;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RateService {

    private final RateClient rateClient;

    public Mono<Flux<CurrencyResponse>> getCurrencies() {
        return rateClient.getCurrencies();
    }

    public Mono<CurrencyResponse> getCurrencyInfo(String code) {
        return rateClient.getCurrencyInfo(code)
                .flatMap(response -> (response instanceof CurrencyResponse)
                        ? Mono.just((CurrencyResponse) response)
                        : getErrorMono((ErrorDTO) response)
                );
    }

    public Mono<RateResponse> getRate(String from, String to, String provider, LocalDate date) {
        return rateClient.getRate(from, to, provider, date)
                .flatMap(response -> (response instanceof RateResponse)
                        ? Mono.just((RateResponse) response)
                        : getErrorMono((ErrorDTO) response)
                );
    }

    public Mono<RateProviderResponse> getRateProviderInfo(String code) {
        return rateClient.getRateProviderInfo(code)
                .flatMap(response -> (response instanceof RateProviderResponse)
                        ? Mono.just((RateProviderResponse) response)
                        : getErrorMono((ErrorDTO) response)
                );
    }

    private static <T> Mono<T> getErrorMono(ErrorDTO errorDTO) {
        return Mono.error(new IllegalArgumentException(errorDTO.getMessage()));
    }

    public Mono<Flux<RateProviderResponse>> getRateProviders() {
        return rateClient.getRateProviders();
    }
}
