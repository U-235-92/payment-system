package aq.project.controllers;

import aq.project.controller.PayoutRestControllerApi;
import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.services.AuthenticationService;
import aq.project.services.PayoutService;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PayoutRestController implements PayoutRestControllerApi {

    private final PayoutService payoutService;

    private final AuthenticationService authenticationService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<PayoutInfoResponseDto> createPayout(
            String authorization,
            CreatePayoutRequestDto createPayoutRequestDto,
            String xTraceId
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        Authentication authentication = authenticationService.getAuthentication();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payoutService.createPayout(createPayoutRequestDto, authentication));
    }

    @Override
    public ResponseEntity<PayoutInfoResponseDto> getPayoutInfo(
            Long id,
            String authorization,
            String xTraceId
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        Authentication authentication = authenticationService.getAuthentication();

        return ResponseEntity.ok(payoutService.getPayoutInfo(id, authentication));
    }

    @Override
    public ResponseEntity<List<PayoutInfoResponseDto>> getPayoutList(
            String authorization,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String xTraceId
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        Authentication authentication = authenticationService.getAuthentication();

        return ResponseEntity.ok(payoutService.getPayoutList(startDate, endDate, authentication));
    }
}
