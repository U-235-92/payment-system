package aq.project.configurations;

import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class ApplicationConfiguration {

    @Value("${spring.application.name}")
    private String serviceName;

    private final OpenTelemetry openTelemetry;

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ApplicationMetricsRegistry applicationMetricsRegistry() {
        return new ApplicationMetricsRegistry(serviceName);
    }
}
