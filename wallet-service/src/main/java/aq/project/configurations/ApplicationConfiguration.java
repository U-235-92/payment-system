package aq.project.configurations;

import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import aq.project.utils.telemetry.ServiceAspectHandler;
import aq.project.utils.telemetry.TraceContext;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy
@RequiredArgsConstructor
@EnableConfigurationProperties
@EnableFeignClients(basePackages = "aq.project.clients")
@EnableJpaRepositories(basePackages = "aq.project.repositories")
public class ApplicationConfiguration {

    @Value("${spring.application.name}")
    private String serviceName;

    private final OpenTelemetry openTelemetry;

    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
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
