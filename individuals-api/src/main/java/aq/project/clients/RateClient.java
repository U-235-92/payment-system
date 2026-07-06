package aq.project.clients;

import aq.project.dto.CurrencyResponse;
import aq.project.dto.ErrorDTO;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static aq.project.util.constants.CustomConstants.ISO_DATE_FORMAT;
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

    public Mono<?> getCurrencyInfo(String code) {
        String requestUrl = String.format("%s/%s", getCurrencyEndpoint, code);
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> currencyRateWebClient.get()
                        .uri(requestUrl)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .exchangeToMono(response -> (isErrorResponse(response))
                            ? response.bodyToMono(ErrorDTO.class)
                            : response.bodyToMono(CurrencyResponse.class)
                        )
                );
    }

    public Mono<?> getRate(String from, String to, String provider, LocalDate date) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> currencyRateWebClient.get()
                        .uri(getRateRequestUrl(from, to, provider, date))
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .exchangeToMono(response -> (isErrorResponse(response))
                                ? response.bodyToMono(ErrorDTO.class)
                                : response.bodyToMono(RateResponse.class)
                        )
                );
    }

    private String getRateRequestUrl(String from, String to, String provider, LocalDate date) {
        String requestUrl = (provider == null || provider.isBlank())
                ? String.format("%s?from=%s&to=%s", getRateEndpoint, from, to)
                : String.format("%s?from=%s&to=%s&provider=%s", getRateEndpoint, from, to, provider);
        requestUrl = (date == null)
                ? requestUrl
                : String.format("%s&date=%s", requestUrl, DateTimeFormatter.ofPattern(ISO_DATE_FORMAT).format(date));
        return requestUrl;
    }

    public Mono<?> getRateProviderInfo(String code) {
        String requestUrl = String.format("%s/%s", getRateProviderEndpoint, code);
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> currencyRateWebClient.get()
                        .uri(requestUrl)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .exchangeToMono(response -> (isErrorResponse(response))
                                ? response.bodyToMono(ErrorDTO.class)
                                : response.bodyToMono(RateProviderResponse.class)
                        )
                );
    }

    public Mono<Flux<RateProviderResponse>> getRateProviders() {
        return jwtClient.requestAdminToken()
                .map(adminAccessToken -> currencyRateWebClient.get()
                        .uri(getRateProvidersEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                        .retrieve()
                        .bodyToFlux(RateProviderResponse.class));
    }

    private boolean isErrorResponse(ClientResponse response) {
        return response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError();
    }
}
