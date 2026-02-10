package aq.project.configs;

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
public class AppConfiguration {

    @Value("${application.keycloak.server-uri}")
    private String keycloakServerURL;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl(keycloakServerURL).build();
    }

    @Bean
    Validator validator() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        return validatorFactory.getValidator();
    }
}
