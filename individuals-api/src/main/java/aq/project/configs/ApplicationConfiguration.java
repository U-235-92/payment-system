package aq.project.configs;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableAspectJAutoProxy
public class ApplicationConfiguration {

    @Value("${keycloak.server-uri}")
    private String keycloakServerURL;

    @Value("${application.person-service.uri}")
    private String personServiceURL;

    @Bean(name = "keycloakWebClient")
    public WebClient keycloakWebClient() {
        return WebClient.builder().baseUrl(keycloakServerURL).build();
    }

    @Bean(name = "personWebClient")
    public WebClient personWebClient() {
        return WebClient.builder().baseUrl(personServiceURL).build();
    }

    @Bean
    Validator validator() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        return validatorFactory.getValidator();
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }
}
