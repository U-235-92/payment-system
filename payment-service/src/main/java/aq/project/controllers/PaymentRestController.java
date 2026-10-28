package aq.project.controllers;

import aq.project.controller.PaymentRestControllerApi;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServicePaymentMethodResponseDto;
import aq.project.services.PaymentService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentRestController implements PaymentRestControllerApi {

    private final PaymentService paymentService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<PaymentServiceCreatePaymentResponseDto> createPayment(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto,
            String xTraceId,
            String authorization
    ) {
        setUpTraceContext(paymentServiceCreatePaymentRequestDto.getTraceId());

        return ResponseEntity.ok(paymentService
                .createPayment(paymentServiceCreatePaymentRequestDto));
    }

    @Override
    public ResponseEntity<List<PaymentServicePaymentMethodResponseDto>> getPaymentMethods(
            String currencyCode,
            String countryCode,
            String xTraceId,
            String authorization
    ) {
        setUpTraceContext(xTraceId);

        return ResponseEntity.ok(paymentService
                .getPaymentMethods(currencyCode, countryCode));
    }

    private void setUpTraceContext(String traceId) {
        traceContext.clean();
        traceContext.setTraceId(traceId);
    }
}