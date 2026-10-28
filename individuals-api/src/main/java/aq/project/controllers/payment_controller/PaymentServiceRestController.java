package aq.project.controllers.payment_controller;

import aq.project.controller.PaymentRestControllerApi;
import aq.project.dto.PaymentServicePaymentMethodResponseDto;
import aq.project.services.payment_service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class PaymentServiceRestController implements PaymentRestControllerApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<Flux<PaymentServicePaymentMethodResponseDto>>> getPaymentMethods(
            String currencyCode,
            String countryCode,
            ServerWebExchange exchange
    ) {
        return paymentService.getPaymentMethods(currencyCode, countryCode)
                .flatMap(response -> Mono.just(ResponseEntity.ok(response)));
    }
}
