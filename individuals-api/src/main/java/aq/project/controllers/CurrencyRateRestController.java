package aq.project.controllers;

import aq.project.controller.RateRestControllerApi;
import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import aq.project.services.RateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CurrencyRateRestController implements RateRestControllerApi {

    private final RateService rateService;

    @Override
    public Mono<ResponseEntity<Flux<CurrencyResponse>>> getCurrencies(ServerWebExchange exchange) {
        return rateService.getCurrencies()
                .flatMap(response -> Mono.just(ResponseEntity.ok().body(response)));
    }

    @Override
    public Mono<ResponseEntity<CurrencyResponse>> getCurrencyInfo(String code, ServerWebExchange exchange) {
        return rateService.getCurrencyInfo(code)
                .flatMap(response -> Mono.just(ResponseEntity.ok().body(response)));
    }

    @Override
    public Mono<ResponseEntity<RateResponse>> getRate(String from, String to, String provider, LocalDate date, ServerWebExchange exchange) {
        return rateService.getRate(from, to, provider, date)
                .flatMap(response -> Mono.just(ResponseEntity.ok().body(response)));
    }

    @Override
    public Mono<ResponseEntity<RateProviderResponse>> getRateProviderInfo(String code, ServerWebExchange exchange) {
        return rateService.getRateProviderInfo(code)
                .flatMap(response -> Mono.just(ResponseEntity.ok().body(response)));
    }

    @Override
    public Mono<ResponseEntity<Flux<RateProviderResponse>>> getRateProviders(ServerWebExchange exchange) {
        return rateService.getRateProviders()
                .flatMap(flux -> Mono.just(ResponseEntity.ok().body(flux)));
    }
}
