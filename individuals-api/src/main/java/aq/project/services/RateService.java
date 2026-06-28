package aq.project.services;

import aq.project.clients.RateClient;
import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RateService {

    private final RateClient rateClient;

    public Mono<Flux<CurrencyResponse>> getCurrencies() {
        return rateClient.getCurrencies();
    }

    public Mono<CurrencyResponse> getCurrencyInfo(String code) {
        return rateClient.getCurrencyInfo(code);
    }

    public Mono<RateResponse> getRate(String from, String to, String provider) {
        return rateClient.getRate(from, to, provider);
    }

    public Mono<RateProviderResponse> getRateProviderInfo(String code) {
        return rateClient.getRateProviderInfo(code);
    }

    public Mono<Flux<RateProviderResponse>> getRateProviders() {
        return rateClient.getRateProviders();
    }
}
