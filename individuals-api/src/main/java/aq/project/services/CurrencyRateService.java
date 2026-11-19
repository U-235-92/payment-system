package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.currency_rate_service.RateApiClient;
import aq.project.dto.CurrencyResponseDto;
import aq.project.dto.RateProviderResponseDto;
import aq.project.dto.RateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;

import static aq.project.telemetry.TracePropagator.fetchTraceId;

@Service
@RequiredArgsConstructor
public class CurrencyRateService {

    private final RateApiClient rateApiClient;

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    public Mono<Flux<CurrencyResponseDto>> getCurrencies() {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminBearerToken()
                        .flatMap(jwtHeader -> rateApiClient.getCurrencies(xTraceId, jwtHeader)
                                .map(response -> Objects.requireNonNull(response.getBody()))));
    }

    public Mono<CurrencyResponseDto> getCurrencyInfo(
            String code
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminBearerToken()
                        .flatMap(jwtHeader -> rateApiClient.getCurrencyInfo(code, xTraceId, jwtHeader)
                                .map(response -> Objects.requireNonNull(response.getBody()))));
    }

    public Mono<RateResponseDto> getRate(
            String from,
            String to,
            String provider,
            OffsetDateTime date
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakServiceClientFacade.getAdminBearerToken()
                        .flatMap(authorization -> rateApiClient.getRate(from, to, provider, date, traceId, authorization)
                                .map(response -> Objects.requireNonNull(response.getBody()))));
    }

    public Mono<RateProviderResponseDto> getRateProviderInfo(
            String code
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminBearerToken()
                        .flatMap(authorization -> rateApiClient.getRateProviderInfo(code, xTraceId, authorization)
                                .map(response -> Objects.requireNonNull(response.getBody()))));
    }

    public Mono<Flux<RateProviderResponseDto>> getRateProviders() {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminBearerToken()
                        .flatMap(authorization -> rateApiClient.getRateProviders(xTraceId, authorization)
                                .map(response -> Objects.requireNonNull(response.getBody()))));
    }
}