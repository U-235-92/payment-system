package aq.project.clients;

import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static aq.project.util.constants.CustomHttpHeaders.BEARER;

@Component
public class RateClient {

    @Value("${application.currency-rate-service.endpoints.get-rate}")
    private String getRateEndpoint;
    @Value("${application.currency-rate-service.endpoints.get-rate-provider}")
    private String getRateProviderEndpoint;
    @Value("${application.currency-rate-service.endpoints.get-rate-providers}")
    private String getRateProvidersEndpoint;
    @Value("${application.currency-rate-service.endpoints.get-currency}")
    private String getCurrencyEndpoint;
    @Value("${application.currency-rate-service.endpoints.get-currencies}")
    private String getCurrenciesEndpoint;

    @Autowired
    private JwtClient jwtClient;

    @Autowired
    @Qualifier("currencyRateServiceWebClient")
    private WebClient currencyRateWebClient;

    public Mono<Flux<CurrencyResponse>> getCurrencies() {
        return jwtClient.requestAdminToken()
                .map(adminAccessToken -> currencyRateWebClient.get()
                        .uri(getCurrenciesEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .retrieve()
                        .bodyToFlux(CurrencyResponse.class));
    }

    public Mono<CurrencyResponse> getCurrencyInfo(String code) {
        String requestUrl = String.format("%s/%s", getCurrencyEndpoint, code);
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> currencyRateWebClient.get()
                        .uri(requestUrl)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .retrieve()
                        .bodyToMono(CurrencyResponse.class));
    }

    public Mono<RateResponse> getRate(String from, String to, String provider) {
        String requestUrl = (provider == null)
                ? String.format("%s?from=%s&to=%s", getRateEndpoint, from, to)
                : String.format("%s?from=%s&to=%s&provider=%s", getRateEndpoint, from, to, provider);
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> currencyRateWebClient.get()
                        .uri(requestUrl)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .retrieve()
                        .bodyToMono(RateResponse.class));
    }

    public Mono<RateProviderResponse> getRateProviderInfo(String code) {
        String requestUrl = String.format("%s/%s", getRateProviderEndpoint, code);
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> currencyRateWebClient.get()
                        .uri(requestUrl)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .retrieve()
                        .bodyToMono(RateProviderResponse.class));
    }

    public Mono<Flux<RateProviderResponse>> getRateProviders() {
        return jwtClient.requestAdminToken()
                .map(adminAccessToken -> currencyRateWebClient.get()
                        .uri(getRateProvidersEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .retrieve()
                        .bodyToFlux(RateProviderResponse.class));
    }
}
