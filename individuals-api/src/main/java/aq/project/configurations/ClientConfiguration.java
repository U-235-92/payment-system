package aq.project.configurations;

import aq.project.clients.KeycloakServiceClient;
import aq.project.currency_rate_service.RateApiClient;
import aq.project.payment_service.PaymentApiClient;
import aq.project.person_service.PersonApiClient;
import aq.project.transaction_service.TransactionApiClient;
import aq.project.utils.client.ClientErrorHandler;
import aq.project.wallet_service.WalletApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class ClientConfiguration {

    @Value("${application.keycloak-service.uri}")
    private String keycloakServiceBaseUrl;

    @Value("${application.person-service.uri}")
    private String personServiceBaseUrl;

    @Value("${application.transaction-service.uri}")
    private String transactionServiceBaseUrl;

    @Value("${application.wallet-service.uri}")
    private String walletServiceBaseUrl;

    @Value("${application.currency-rate-service.uri}")
    private String currencyRateServiceBaseUrl;

    @Value("${application.payment-service.uri}")
    private String paymentServiceBaseUrl;

    @Bean(name = "keycloakWebClient")
    public WebClient keycloakWebClient() {
        return WebClient.builder()
                .baseUrl(keycloakServiceBaseUrl)
                .build();
    }

    @Bean
    public KeycloakServiceClient keycloakServiceWebClient(Environment env) {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloakServiceBaseUrl)
                .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onWebClientErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .embeddedValueResolver(env::resolvePlaceholders)
                .build();

        return factory.createClient(KeycloakServiceClient.class);
    }

    @Bean
    public PersonApiClient personApiClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(personServiceBaseUrl)
                .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onWebClientErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(PersonApiClient.class);
    }

    @Bean
    public TransactionApiClient transactionApiClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(transactionServiceBaseUrl)
                .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onWebClientErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(TransactionApiClient.class);
    }

    @Bean
    public WalletApiClient walletApiClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(walletServiceBaseUrl)
                .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onWebClientErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(WalletApiClient.class);
    }

    @Bean
    public RateApiClient rateApiClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(currencyRateServiceBaseUrl)
                .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onWebClientErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(RateApiClient.class);
    }

    @Bean
    public PaymentApiClient paymentApiClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(paymentServiceBaseUrl)
                .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onWebClientErrorResponse)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(PaymentApiClient.class);
    }
}