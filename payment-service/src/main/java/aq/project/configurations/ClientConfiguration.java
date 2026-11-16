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
        group = "fake-payment-provider-service",
        types = {
                aq.project.fake_payment_provider_service.TransactionApiClient.class,
                aq.project.fake_payment_provider_service.PayoutApiClient.class
        }
)
@RequiredArgsConstructor
public class ClientConfiguration {

    @Value("${app.service.fake-payment-provider-service.uri}")
    private String fakePaymentProviderServiceBaseUri;

    @Bean
    public RestClientHttpServiceGroupConfigurer restClientHttpServiceGroupConfigurer() {
        return groups -> {
            groups.forEachGroup((group, clientBuilder, factoryBuilder) -> {
                switch(group.name()) {
                    case "fake-payment-provider-service" -> clientBuilder
                            .baseUrl(fakePaymentProviderServiceBaseUri)
                            .defaultStatusHandler(ClientErrorHandler::isErrorResponse, ClientErrorHandler::onClientErrorResponse)
                            .build();
                }
            });
        };
    }
}