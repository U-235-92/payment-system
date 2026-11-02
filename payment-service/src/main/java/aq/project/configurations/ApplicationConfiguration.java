package aq.project.configurations;

import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import aq.project.utils.telemetry.ServiceAspectHandler;
import aq.project.utils.telemetry.TraceContext;
import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class ApplicationConfiguration {

    @Value("${spring.application.name}")
    private String serviceName;

    private final OpenTelemetry openTelemetry;

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ControllerExceptionLogger controllerExceptionLogger(TraceContext traceContext) {
        return new ControllerExceptionLogger(traceContext);
    }

    @Bean
    public ServiceAspectHandler serviceAspectHandler() {
        return new ServiceAspectHandler(openTelemetry, traceContext(), applicationMetricsRegistry());
    }

    @Bean
    public ApplicationMetricsRegistry applicationMetricsRegistry() {
        return new ApplicationMetricsRegistry(serviceName);
    }

    @Bean
    public Fallback fallback() {
        return new Fallback(serviceName, openTelemetry, traceContext());
    }

    @Bean
    public TraceContext traceContext() {
        return new TraceContext();
    }
}
