package aq.project.configurations;

import aq.project.clients.*;
import aq.project.dto.ErrorDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;

import static aq.project.util.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;

@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

    @Value("${application.keycloak-service.uri}")
    private String keycloakServiceBaseUrl;
    @Value("${application.person-service.uri}")
    private String personServiceBaseUrl;
    @Value("${application.currency-rate-service.uri}")
    private String currencyRateServiceBaseUrl;
    @Value("${application.transaction-service.uri}")
    private String transactionServiceBaseUrl;
    @Value("${application.wallet-service.uri}")
    private String walletServiceBaseUrl;

    @Bean(name = "keycloakWebClient")
    public WebClient keycloakWebClient() {
        return WebClient.builder()
                .baseUrl(keycloakServiceBaseUrl)
                .build();
    }

    @Bean
    public KeycloakServiceWebClient keycloakServiceWebClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloakServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, response -> Mono.error(new HttpClientErrorException(response.statusCode())))
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, response -> Mono.error(new HttpServerErrorException(response.statusCode())))
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(KeycloakServiceWebClient.class);
    }

    @Bean
    public PersonServiceWebClient personServiceWebClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(personServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::doOn4xxClientError)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::doOn5xxServerError)
                .filter(propagateTraceId())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(PersonServiceWebClient.class);
    }

    @Bean
    public CurrencyRateServiceWebClient currencyRateServiceWebClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(currencyRateServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::doOn4xxClientError)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::doOn5xxServerError)
                .filter(propagateTraceId())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(CurrencyRateServiceWebClient.class);
    }

    @Bean
    public TransactionServiceWebClient transactionServiceWebClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(transactionServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::doOn4xxClientError)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::doOn5xxServerError)
                .filter(propagateTraceId())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(TransactionServiceWebClient.class);
    }

    @Bean
    public WalletServiceWebClient walletServiceWebClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(walletServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::doOn4xxClientError)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::doOn5xxServerError)
                .filter(propagateTraceId())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(WalletServiceWebClient.class);
    }

    private Mono<? extends Exception> doOn4xxClientError(ClientResponse response) {
        return response.bodyToMono(ErrorDTO.class)
                .map(dto -> new HttpClientErrorException(HttpStatusCode.valueOf(dto.getHttpStatus()), dto.getMessage()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new HttpClientErrorException(response.statusCode()))));
    }

    private Mono<? extends Exception> doOn5xxServerError(ClientResponse response) {
        return response.bodyToMono(ErrorDTO.class)
                .map(dto -> new HttpServerErrorException(HttpStatusCode.valueOf(dto.getHttpStatus()), dto.getMessage()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new HttpServerErrorException(response.statusCode()))));
    }

    private ExchangeFilterFunction propagateTraceId() {
        return (request, chain) ->
                Mono.deferContextual(context -> {
                    final String traceId = context.getOrDefault(X_TRACE_ID_HEADER, getDefaultTraceId());
                    return chain.exchange(ClientRequest
                            .from(request)
                            .header(X_TRACE_ID_HEADER, traceId)
                            .build()
                    );
                });
    }

    private String getDefaultTraceId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder traceId = new StringBuilder();
        for(byte b : bytes) {
            traceId.append(String.format("%02x", b));
        }
        return traceId.toString();
    }
}
