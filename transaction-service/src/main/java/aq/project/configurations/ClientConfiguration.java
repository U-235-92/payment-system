package aq.project.configurations;

import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.ServiceHttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@ImportHttpServices(
        group = "wallet-service",
        types = aq.project.wallet_service.WalletApiClient.class
)
@ImportHttpServices(
        group = "wallet-service",
        types = aq.project.wallet_service.TransactionApiClient.class
)
@ImportHttpServices(
        group = "keycloak-service",
        types = aq.project.clients.KeycloakServiceRestClient.class
)
@ImportHttpServices(
        group = "payment-provider-service",
        types = aq.project.payment_provider_service.WebhookApiClient.class
)
@ImportHttpServices(
        group = "payment-provider-service",
        types = aq.project.payment_provider_service.TransactionApiClient.class
)
public class ClientConfiguration {

    @Value("${service.keycloak.uri}")
    private String keycloakServiceBaseUri;

    @Value("${service.wallet-service.uri}")
    private String walletServiceBaseUri;

    @Value("${service.payment-provider-service.uri}")
    private String paymentProviderServiceBaseUri;

    @Bean
    public RestClientHttpServiceGroupConfigurer restClientHttpServiceGroupConfigurer() {
        return groups -> {
            groups.forEachGroup((group, clientBuilder, factoryBuilder) -> {
                switch(group.name()) {
                    case "wallet-service" -> clientBuilder
                            .baseUrl(walletServiceBaseUri)
                            .defaultStatusHandler(this::isErrorResponse, this::doOnForeignServiceErrorResponse)
                            .build();
                    case "keycloak-service" -> clientBuilder
                            .baseUrl(keycloakServiceBaseUri)
                            .defaultStatusHandler(this::isErrorResponse, this::doOnForeignServiceErrorResponse)
                            .build();
                    case "payment-provider-service" -> clientBuilder
                            .baseUrl(paymentProviderServiceBaseUri)
                            .defaultStatusHandler(this::isErrorResponse, this::doOnForeignServiceErrorResponse)
                            .build();
                }
            });
        };
    }

    private boolean isErrorResponse(HttpStatusCode statusCode) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }

    private void doOnForeignServiceErrorResponse(HttpRequest request, ClientHttpResponse response) {
        try {
            HttpStatusCode status = response.getStatusCode();
            if(status.is4xxClientError())
                throw new ClientHttpException(status.value(), "");
            if(status.is5xxServerError())
                throw new ServiceHttpException(status.value(), "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
