package aq.project.services;

import aq.project.clients.CurrencyRateServiceWebClient;
import aq.project.clients.KeycloakServiceWebClientFacade;
import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static aq.project.util.constants.CustomConstants.ISO_DATE_FORMAT;

@Service
@RequiredArgsConstructor
public class CurrencyRateService {

    private final CurrencyRateServiceWebClient currencyRateServiceWebClient;

    private final KeycloakServiceWebClientFacade keycloakServiceWebClientFacade;

    public Mono<Flux<CurrencyResponse>> getCurrencies() {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> currencyRateServiceWebClient.getCurrencies(jwt)
                        .flatMap(response -> Mono.just(response.getBody())));
    }

    public Mono<CurrencyResponse> getCurrencyInfo(String code) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> currencyRateServiceWebClient.getCurrencyInfo(jwt, code)
                        .flatMap(response -> Mono.just(response.getBody())));
    }

    public Mono<RateResponse> getRate(String from, String to, String provider, LocalDate date) {
        String strDate = (date == null)
                ? null
                : DateTimeFormatter.ofPattern(ISO_DATE_FORMAT).format(date);
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> currencyRateServiceWebClient.getRate(jwt, from, to, provider, strDate)
                        .flatMap(response -> Mono.just(response.getBody())));
    }

    public Mono<RateProviderResponse> getRateProviderInfo(String code) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> currencyRateServiceWebClient.getRateProviderInfo(jwt, code)
                        .flatMap(response -> Mono.just(response.getBody())));
    }

    public Mono<Flux<RateProviderResponse>> getRateProviders() {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> currencyRateServiceWebClient.getRateProviders(jwt)
                        .flatMap(response -> Mono.just(response.getBody())));
    }
}
