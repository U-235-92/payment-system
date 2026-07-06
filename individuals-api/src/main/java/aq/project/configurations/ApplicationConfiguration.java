package aq.project.configurations;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.security.SecureRandom;
import static aq.project.util.constants.CustomHttpHeaders.*;

@Configuration
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class ApplicationConfiguration {

    @Value("${application.keycloak-service.uri}")
    private String keycloakServiceURL;

    @Value("${application.person-service.uri}")
    private String personServiceURL;

    @Value("${application.wallet-service.uri}")
    private String walletServiceURL;

    @Value("${application.transaction-service.uri}")
    private String transactionServiceURL;

    @Value("${application.currency-rate-service.uri}")
    private String currencyRateServiceURL;

    @Bean(name = "keycloakServiceWebClient")
    public WebClient keycloakWebClient() {
        return WebClient.builder()
                .baseUrl(keycloakServiceURL)
                .build();
    }

    @Bean(name = "personServiceWebClient")
    public WebClient personWebClient() {
        return WebClient.builder()
                .baseUrl(personServiceURL)
                .filter(tracePropagationFilter())
                .build();
    }

    @Bean(name = "walletServiceWebClient")
    public WebClient walletWebClient() {
        return WebClient.builder()
                .baseUrl(walletServiceURL)
                .filter(tracePropagationFilter())
                .build();
    }

    @Bean(name = "transactionServiceWebClient")
    public WebClient transactionWebClient() {
        return WebClient.builder()
                .baseUrl(transactionServiceURL)
                .filter(tracePropagationFilter())
                .build();
    }

    @Bean(name = "currencyRateServiceWebClient")
    public WebClient currencyRateWebClient() {
        return WebClient.builder()
                .baseUrl(currencyRateServiceURL)
                .filter(tracePropagationFilter())
                .build();
    }

    private ExchangeFilterFunction tracePropagationFilter() {
        return (request, chain) ->
            Mono.deferContextual(context -> {
                final String traceId = context.getOrDefault(X_TRACE_ID_HEADER, getDefaultTraceId());
                return chain.exchange(ClientRequest
                    .from(request)
                    .header(X_TRACE_ID_HEADER, traceId)
                    .build()
                );
            });
    }

    private String getDefaultTraceId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder traceId = new StringBuilder();
        for(byte b : bytes) {
            traceId.append(String.format("%02x", b));
        }
        return traceId.toString();
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }
}
