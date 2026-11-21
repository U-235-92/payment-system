package aq.project.controllers;

import aq.project.controller.PaymentRestControllerApi;
import aq.project.dto.CreatePaymentRequestDto;
import aq.project.dto.CreatePaymentResponseDto;
import aq.project.dto.PaymentMethodResponseDto;
import aq.project.services.PaymentService;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentRestController implements PaymentRestControllerApi {

    private final PaymentService paymentService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<CreatePaymentResponseDto> createPayment(
            CreatePaymentRequestDto paymentServiceCreatePaymentRequestDto,
            String xTraceId,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(paymentServiceCreatePaymentRequestDto));
    }

    @Override
    public ResponseEntity<List<PaymentMethodResponseDto>> getPaymentMethods(
            String currencyCode,
            String countryCode,
            String xTraceId,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.ok(paymentService.getPaymentMethods(currencyCode, countryCode));
    }
}