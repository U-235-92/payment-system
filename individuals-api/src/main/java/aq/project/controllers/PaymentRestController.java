package aq.project.controllers;

import aq.project.controller.PaymentRestControllerApi;
import aq.project.dto.PaymentMethodResponseDto;
import aq.project.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class PaymentRestController implements PaymentRestControllerApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<Flux<PaymentMethodResponseDto>>> getPaymentMethods(
            String currencyCode,
            String countryCode,
            ServerWebExchange exchange
    ) {
        return paymentService.getPaymentMethods(currencyCode, countryCode)
                .map(ResponseEntity::ok);
    }
}
