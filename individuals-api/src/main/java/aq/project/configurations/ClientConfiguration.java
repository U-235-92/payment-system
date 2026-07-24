package aq.project.configurations;

import aq.project.clients.KeycloakServiceClient;
import aq.project.currency_rate_service.RateApiClient;
import aq.project.dto.ErrorDto;
import aq.project.person_service.PersonApiClient;
import aq.project.transaction_service.TransactionApiClient;
import aq.project.wallet_service.WalletApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class ClientConfiguration {

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
    public KeycloakServiceClient keycloakServiceWebClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloakServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, response -> Mono.error(new HttpClientErrorException(response.statusCode())))
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, response -> Mono.error(new HttpServerErrorException(response.statusCode())))
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(KeycloakServiceClient.class);
    }

    @Bean
    public PersonApiClient personApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(personServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::doOn4xxClientError)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::doOn5xxServerError)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(PersonApiClient.class);
    }

    @Bean
    public RateApiClient rateApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(currencyRateServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::doOn4xxClientError)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::doOn5xxServerError)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(RateApiClient.class);
    }

    @Bean
    public TransactionApiClient transactionApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(transactionServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::doOn4xxClientError)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::doOn5xxServerError)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(TransactionApiClient.class);
    }

    @Bean
    public WalletApiClient walletApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(walletServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::doOn4xxClientError)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::doOn5xxServerError)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(WalletApiClient.class);
    }

    private Mono<? extends Exception> doOn4xxClientError(ClientResponse response) {
        return response.bodyToMono(ErrorDto.class)
                .map(dto -> new HttpClientErrorException(HttpStatusCode.valueOf(dto.getHttpStatus()), dto.getMessage()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new HttpClientErrorException(response.statusCode()))));
    }

    private Mono<? extends Exception> doOn5xxServerError(ClientResponse response) {
        return response.bodyToMono(ErrorDto.class)
                .map(dto -> new HttpServerErrorException(HttpStatusCode.valueOf(dto.getHttpStatus()), dto.getMessage()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new HttpServerErrorException(response.statusCode()))));
    }
}