package aq.project.configurations;

import aq.project.clients.FrankfurterRateProviderClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@RequiredArgsConstructor
@ImportHttpServices(group = "currency-rate-service", types = FrankfurterRateProviderClient.class)
public class RestClientConfiguration {

    @Value("${application.client.frankfurter.base-url}")
    private String currencyRateServiceBaseUrl;

    @Bean
    public RestClientHttpServiceGroupConfigurer restClientHttpServiceGroupConfigurer() {
        return groups -> {
            groups.forEachGroup((group, clientBuilder, factoryBuilder) -> {
                switch (group.name()) {
                    case "currency-rate-service" -> clientBuilder.baseUrl(currencyRateServiceBaseUrl).build();
                }
            });
        };
    }
}
