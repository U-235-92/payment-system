package aq.project.configurations;

import aq.project.clients.KeycloakServiceRestClient;
import aq.project.dto.ErrorDto;
import aq.project.exceptions.TransactionException;
import aq.project.wallet_service.TransactionApiClient;
import aq.project.wallet_service.WalletApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@ImportHttpServices(group = "wallet-service", types = WalletApiClient.class)
@ImportHttpServices(group = "wallet-service", types = TransactionApiClient.class)
@ImportHttpServices(group = "keycloak-service", types = KeycloakServiceRestClient.class)
public class ClientConfiguration {

    @Value("${service.keycloak.uri}")
    private String keycloakServiceBaseUri;
    @Value("${service.wallet-service.uri}")
    private String walletServiceBaseUri;
    @Value("${service.wallet-service.endpoints.get-transaction-status}")
    private String walletServiceEndpointGetTransactionStatus;

    private final ObjectMapper objectMapper;

    @Bean
    public RestClientHttpServiceGroupConfigurer restClientHttpServiceGroupConfigurer() {
        return groups -> {
            groups.forEachGroup((group, clientBuilder, factoryBuilder) -> {
                switch(group.name()) {
                    case "wallet-service" -> clientBuilder
                            .baseUrl(walletServiceBaseUri)
                            .defaultStatusHandler(this::isErrorResponse, this::doOnWalletServiceErrorResponse)
                            .build();
                    case "keycloak-service" -> clientBuilder
                            .baseUrl(keycloakServiceBaseUri)
                            .build();
                }
            });
        };
    }

    private boolean isErrorResponse(HttpStatusCode statusCode) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }

    private void doOnWalletServiceErrorResponse(HttpRequest request, ClientHttpResponse response) {
        if(isTransactionStatusRequest(request)) {
            try {
                ErrorDto errorDto = objectMapper.readValue(response.getBody(), ErrorDto.class);
                if(errorDto != null)
                    throw new TransactionException(errorDto.getMessage());
                String msg = String.format("Unexpected error occurred during getting transaction status. Status code: %d",
                        response.getStatusCode().value());
                throw new TransactionException(msg);
            } catch (IOException e) {
                String msg = "Unexpected error occurred during process error response of transaction status request";
                throw new IllegalStateException(msg, e);
            }
        }
    }

    private boolean isTransactionStatusRequest(HttpRequest request) {
        return request.getURI().getPath().contains(walletServiceEndpointGetTransactionStatus);
    }
}
