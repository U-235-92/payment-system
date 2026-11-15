package aq.project.configurations;

import aq.project.utils.client.ClientErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import tools.jackson.databind.ObjectMapper;

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
                            .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onClientErrorResponse)
                            .build();

                    case "keycloak-service" -> clientBuilder
                            .baseUrl(keycloakServiceBaseUri)
                            .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onClientErrorResponse)
                            .build();
                }
            });
        };
    }
}