package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.CreatePaymentRequestDto;
import aq.project.dto.CreatePaymentResponseDto;
import aq.project.dto.PaymentMethodResponseDto;
import aq.project.dto.TransactionType;
import aq.project.payment_service.PaymentApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static aq.project.telemetry.TracePropagator.fetchTraceId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final KeycloakServiceClientFacade keycloakClient;

    private final PaymentApiClient paymentClient;

    public Mono<Flux<PaymentMethodResponseDto>> getPaymentMethods(
            String currencyCode,
            String countryCode
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> paymentClient.getPaymentMethods(currencyCode, countryCode, traceId, authorization)
                                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())))));
    }

    Mono<CreatePaymentResponseDto> createPayment(
            String traceId,
            String authorization,
            UUID transactionId,
            Long paymentMethodId,
            TransactionType transactionType,
            BigDecimal amount,
            String currencyCode
    ) {
        CreatePaymentRequestDto createPaymentRequestDto = new CreatePaymentRequestDto()
                .transactionId(transactionId)
                .paymentMethodId(paymentMethodId)
                .transactionType(transactionType)
                .amount(amount)
                .currencyCode(currencyCode);

        return paymentClient.createPayment(Mono.just(createPaymentRequestDto), traceId, authorization)
                .map(response -> Objects.requireNonNull(response.getBody()));
    }
}