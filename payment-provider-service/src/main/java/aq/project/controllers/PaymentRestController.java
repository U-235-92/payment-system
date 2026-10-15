package aq.project.controllers;

import aq.project.controller.PaymentRestControllerApi;
import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentProviderServiceFailPaymentRequestDto;
import aq.project.services.PaymentService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentRestController implements PaymentRestControllerApi {

    private final PaymentService paymentService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<PaymentProviderServiceCreatePaymentResponseDto> createPayment(
            String authorization,
            PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto,
            String xTraceId
    ) {
        setUpTraceContext(xTraceId);

        return ResponseEntity.ok(paymentService
                .createPayment(paymentProviderServiceCreatePaymentRequestDto));
    }

    @Override
    public ResponseEntity<Void> failPayment(
            String authorization,
            PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto,
            String xTraceId
    ) {
        setUpTraceContext(xTraceId);

        paymentService.failPayment(paymentProviderServiceFailPaymentRequestDto);

        return ResponseEntity.ok().build();
    }

    private void setUpTraceContext(String traceId) {
        traceContext.clean();
        traceContext.setTraceId(traceId);
    }
}
