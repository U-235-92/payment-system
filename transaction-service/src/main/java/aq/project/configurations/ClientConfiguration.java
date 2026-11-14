package aq.project.configurations;

import aq.project.dto.WalletServiceErrorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Configuration
@ImportHttpServices(
        group = "wallet-service",
        types = aq.project.wallet_service.WalletApiClient.class
)
@ImportHttpServices(
        group = "keycloak-service",
        types = aq.project.clients.KeycloakServiceRestClient.class
)
@RequiredArgsConstructor
public class ClientConfiguration {

    @Value("${app.service.keycloak.uri}")
    private String keycloakServiceBaseUri;

    @Value("${app.service.wallet-service.uri}")
    private String walletServiceBaseUri;

    private final ObjectMapper objectMapper;

    @Bean
    public RestClientHttpServiceGroupConfigurer restClientHttpServiceGroupConfigurer() {
        return groups -> {
            groups.forEachGroup((group, clientBuilder, factoryBuilder) -> {
                switch(group.name()) {
                    case "wallet-service" -> clientBuilder
                            .baseUrl(walletServiceBaseUri)
                            .defaultStatusHandler(this::isErrorResponse, this::onWalletServiceErrorResponse)
                            .build();

                    case "keycloak-service" -> clientBuilder
                            .baseUrl(keycloakServiceBaseUri)
                            .defaultStatusHandler(this::isErrorResponse, this::onKeycloakServiceErrorResponse)
                            .build();
                }
            });
        };
    }

    private boolean isErrorResponse(HttpStatusCode statusCode) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }

    private void onWalletServiceErrorResponse(
            HttpRequest request,
            ClientHttpResponse response
    ) throws IOException {
        WalletServiceErrorDto errorDto = objectMapper
                .readValue(response.getBody(), WalletServiceErrorDto.class);

        HttpStatusCode responseStatusCode = response.getStatusCode();

        if(responseStatusCode.is4xxClientError())
            throw new HttpClientErrorException(responseStatusCode, errorDto.getMessage());

        if(responseStatusCode.is5xxServerError())
            throw new HttpServerErrorException(responseStatusCode, errorDto.getMessage());
    }

    private void onKeycloakServiceErrorResponse(
            HttpRequest request,
            ClientHttpResponse response
    ) throws IOException {
        HttpStatusCode status = response.getStatusCode();

        if(status.is4xxClientError())
            throw new HttpClientErrorException(status, "");

        if(status.is5xxServerError())
            throw new HttpServerErrorException(status, "");
    }
}
