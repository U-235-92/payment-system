package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.PaymentMethodResponseDto;
import aq.project.payment_service.PaymentApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static aq.project.telemetry.TracePropagator.fetchTraceId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final KeycloakServiceClientFacade keycloakClient;

    private final PaymentApiClient paymentApiClient;

    public Mono<Flux<PaymentMethodResponseDto>> getPaymentMethods(
            String currencyCode,
            String countryCode
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> paymentApiClient.getPaymentMethods(currencyCode, countryCode, traceId, authorization)
                                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())))));
    }
}