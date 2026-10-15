package aq.project.configurations;

import aq.project.clients.KeycloakServiceClient;
import aq.project.currency_rate_service.RateApiClient;
import aq.project.dto.*;
import aq.project.payment_service.PaymentApiClient;
import aq.project.person_service.PersonApiClient;
import aq.project.transaction_service.DepositTransactionApiClient;
import aq.project.transaction_service.TransferTransactionApiClient;
import aq.project.transaction_service.WithdrawTransactionApiClient;
import aq.project.wallet_service.WalletApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
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
    @Value("${application.payment-service.uri}")
    private String paymentServiceBaseUrl;

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
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::on4xxKeycloakServiceErrorResponse)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::on5xxKeycloakServiceErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(KeycloakServiceClient.class);
    }

    private Mono<? extends Exception> on4xxKeycloakServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return Mono.error(getHttpClientErrorException(httpStatusCode, null));
    }

    private Mono<? extends Exception> on5xxKeycloakServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return Mono.error(getHttpServerErrorException(httpStatusCode, null));
    }

    @Bean
    public PersonApiClient personApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(personServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::on4xxPersonServiceErrorResponse)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::on5xxPersonServiceErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(PersonApiClient.class);
    }

    private Mono<? extends Exception> on4xxPersonServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(PersonServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpClientErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpClientErrorException(httpStatusCode, dto.getMessage())));
    }

    private Mono<? extends Exception> on5xxPersonServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(PersonServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpServerErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpServerErrorException(httpStatusCode, dto.getMessage())));
    }

    @Bean
    public RateApiClient rateApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(currencyRateServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::on4xxCurrencyRateServiceErrorResponse)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::on5xxCurrencyRateServiceErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(RateApiClient.class);
    }

    private Mono<? extends Exception> on4xxCurrencyRateServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(CurrencyRateServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpClientErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpClientErrorException(httpStatusCode, dto.getMessage())));
    }

    private Mono<? extends Exception> on5xxCurrencyRateServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(CurrencyRateServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpServerErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpServerErrorException(httpStatusCode, dto.getMessage())));
    }

    @Bean
    public DepositTransactionApiClient depositTransactionApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(transactionServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::on4xxTransactionServiceErrorResponse)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::on5xxTransactionServiceErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(DepositTransactionApiClient.class);
    }

    @Bean
    public TransferTransactionApiClient transferTransactionApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(transactionServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::on4xxTransactionServiceErrorResponse)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::on5xxTransactionServiceErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(TransferTransactionApiClient.class);
    }

    @Bean
    public WithdrawTransactionApiClient withdrawTransactionApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(transactionServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::on4xxTransactionServiceErrorResponse)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::on5xxTransactionServiceErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(WithdrawTransactionApiClient.class);
    }

    private Mono<? extends Exception> on4xxTransactionServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(TransactionServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpClientErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpClientErrorException(httpStatusCode, dto.getMessage())));
    }

    private Mono<? extends Exception> on5xxTransactionServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(TransactionServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpServerErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpServerErrorException(httpStatusCode, dto.getMessage())));
    }

    @Bean
    public WalletApiClient walletApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(walletServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::on4xxWalletServiceErrorResponse)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::on5xxWalletServiceErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(WalletApiClient.class);
    }

    private Mono<? extends Exception> on4xxWalletServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(WalletServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpClientErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpClientErrorException(httpStatusCode, dto.getMessage())));
    }

    private Mono<? extends Exception> on5xxWalletServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(WalletServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpServerErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpServerErrorException(httpStatusCode, dto.getMessage())));
    }

    @Bean
    public PaymentApiClient paymentApiClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(paymentServiceBaseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this::on4xxPaymentServiceErrorResponse)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, this::on5xxPaymentServiceErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(PaymentApiClient.class);
    }

    private Mono<? extends Exception> on4xxPaymentServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(PaymentServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpClientErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpClientErrorException(httpStatusCode, dto.getMessage())));
    }

    private Mono<? extends Exception> on5xxPaymentServiceErrorResponse(ClientResponse response) {
        HttpStatusCode httpStatusCode = response.statusCode();

        return response.bodyToMono(PaymentServiceErrorDto.class)
                .switchIfEmpty(Mono.error(() -> getHttpServerErrorException(httpStatusCode, null)))
                .flatMap(dto ->
                        Mono.error(getHttpServerErrorException(httpStatusCode, dto.getMessage())));
    }

    private HttpClientErrorException getHttpClientErrorException(HttpStatusCode httpStatusCode, String message) {
        httpStatusCode = (httpStatusCode == null)
                ? HttpStatus.BAD_REQUEST
                : httpStatusCode;

        return message == null
                ? new HttpClientErrorException(httpStatusCode)
                : new HttpClientErrorException(httpStatusCode, message);
    }

    private HttpServerErrorException getHttpServerErrorException(HttpStatusCode httpStatusCode, String message) {
        httpStatusCode = (httpStatusCode == null)
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : httpStatusCode;

        return message == null
                ? new HttpServerErrorException(httpStatusCode)
                : new HttpServerErrorException(httpStatusCode, message);
    }
}