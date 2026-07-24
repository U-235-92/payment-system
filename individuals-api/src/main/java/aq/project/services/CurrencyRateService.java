package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.currency_rate_service.RateApiClient;
import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static aq.project.utils.telemetry.TracePropagator.fetchTraceId;

@Service
@RequiredArgsConstructor
public class CurrencyRateService {

    private final RateApiClient rateApiClient;

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    public Mono<Flux<CurrencyResponse>> getCurrencies() {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> rateApiClient.getCurrencies(xTraceId, jwtHeader)
                                .flatMap(response -> Mono.just(response.getBody()))));
    }

    public Mono<CurrencyResponse> getCurrencyInfo(
            String code
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> rateApiClient.getCurrencyInfo(code, xTraceId, jwtHeader)
                                .map(ResponseEntity::getBody)));
    }

    public Mono<RateResponse> getRate(
            String from,
            String to,
            String provider,
            OffsetDateTime date
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> rateApiClient.getRate(from, to, xTraceId, provider, date, jwtHeader)
                                .map(ResponseEntity::getBody)));
    }

    public Mono<RateProviderResponse> getRateProviderInfo(
            String code
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> rateApiClient.getRateProviderInfo(code, xTraceId, jwtHeader)
                                .map(ResponseEntity::getBody)));
    }

    public Mono<Flux<RateProviderResponse>> getRateProviders() {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> rateApiClient.getRateProviders(xTraceId, jwtHeader)
                                .map(ResponseEntity::getBody)));
    }
}
