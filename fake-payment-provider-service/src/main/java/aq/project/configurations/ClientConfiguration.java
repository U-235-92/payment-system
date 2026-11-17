package aq.project.configurations;

import aq.project.utils.client.ClientErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "webhook-service",
        types = aq.project.webhook_service.WebhookApiClient.class
)
@RequiredArgsConstructor
public class ClientConfiguration {

    @Value("${app.service.webhook-service.uri}")
    private String webhookServiceBaseUri;

    @Bean
    public RestClientHttpServiceGroupConfigurer restClientHttpServiceGroupConfigurer() {
        return groups -> {
            groups.forEachGroup((group, clientBuilder, factoryBuilder) -> {
                switch(group.name()) {
                    case "webhook-service" -> clientBuilder
                            .baseUrl(webhookServiceBaseUri)
                            .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onClientErrorResponse)
                            .build();
                }
            });
        };
    }
}