package aq.project.clients;

import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public interface CurrencyRateServiceWebClient {

    @GetExchange(
            value = "${application.currency-rate-service.endpoints.get-currencies}"
    )
    Mono<ResponseEntity<Flux<CurrencyResponse>>> getCurrencies(
            @RequestHeader(name = AUTHORIZATION) String adminJwt
    );

    @GetExchange(
            value = "${application.currency-rate-service.endpoints.get-currency}/{code}"
    )
    Mono<ResponseEntity<CurrencyResponse>> getCurrencyInfo(
            @RequestHeader(name = AUTHORIZATION) String adminJwt,
            @PathVariable("code") String code
    );

    @GetExchange(
            value = "${application.currency-rate-service.endpoints.get-rate}"
    )
    Mono<ResponseEntity<RateResponse>> getRate(
            @RequestHeader(name = AUTHORIZATION) String adminJwt,
            @RequestParam(name = "from") String from,
            @RequestParam(name = "to") String to,
            @RequestParam(name = "provider", required = false) String provider,
            @RequestParam(name = "date", required = false) String date
    );

    @GetExchange(
            value = "${application.currency-rate-service.endpoints.get-rate-provider}/{code}"
    )
    Mono<ResponseEntity<RateProviderResponse>> getRateProviderInfo(
            @RequestHeader(name = AUTHORIZATION) String adminJwt,
            @PathVariable("code") String code
    );

    @GetExchange(
            value = "${application.currency-rate-service.endpoints.get-rate-providers}"
    )
    Mono<ResponseEntity<Flux<RateProviderResponse>>> getRateProviders(
            @RequestHeader(name = AUTHORIZATION) String adminJwt
    );
}
