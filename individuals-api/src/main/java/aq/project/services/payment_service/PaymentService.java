package aq.project.services.payment_service;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.PaymentServicePaymentMethodResponseDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.payment_service.PaymentApiClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static aq.project.utils.telemetry.TracePropagator.fetchTraceId;
import static aq.project.utils.telemetry.TracePropagator.genTraceId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${spring.application.name}")
    private String serviceName;

    private final PaymentApiClient paymentApiClient;

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    @RateLimiter(
            name = "get-payment-methods-rate-limiter",
            fallbackMethod = "getPaymentMethodsFallback"
    )
    @CircuitBreaker(
            name = "get-payment-methods-circuitbreaker",
            fallbackMethod = "getPaymentMethodsFallback"
    )
    public Mono<Flux<PaymentServicePaymentMethodResponseDto>> getPaymentMethods(
            String currencyCode,
            String countryCode
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> paymentApiClient.getPaymentMethods(currencyCode, countryCode, traceId, jwtHeader)
                                .flatMap(response -> Mono.just(response.getBody()))));

    }

    public Mono<Flux<PaymentServicePaymentMethodResponseDto>> getPaymentMethodsFallback(
            String currencyCode,
            String countryCode,
            Exception exception
    ) {
        if(exception instanceof ConstraintViolationException)
            return Mono.error(() -> exception);

        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, genTraceId());
            String action = "get-payment-methods-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error(
                    "[{}][{} -> {}]: {} occurred while getting payment methods " +
                    "for currency code: [{}] and country code: [{}]. Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, currencyCode, countryCode, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }
}
