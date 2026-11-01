package aq.project.configurations;

import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import aq.project.utils.telemetry.ServiceAspectHandler;
import aq.project.utils.telemetry.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class ApplicationConfiguration {

    @Value("${spring.application.name}")
    private String serviceName;

    private final OpenTelemetry openTelemetry;

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
    public TraceContext traceContext() {
        return new TraceContext();
    }
}
