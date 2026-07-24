package aq.project.controllers;

import aq.project.controller.RateRestControllerApi;
import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import aq.project.services.RateService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RateRestController implements RateRestControllerApi {

    private final RateService rateService;
    private final TraceContext traceContext;

    @Override
    public ResponseEntity<List<CurrencyResponse>> getCurrencies(
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getCurrencies());
    }

    @Override
    public ResponseEntity<RateResponse> getRate(
            String from,
            String to,
            String xTraceId,
            String provider,
            OffsetDateTime date,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getRate(from, to, provider, date));
    }

    @Override
    public ResponseEntity<List<RateProviderResponse>> getRateProviders(
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getRateProviders());
    }

    @Override
    public ResponseEntity<CurrencyResponse> getCurrencyInfo(
            String code,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getCurrencyInfo(code));
    }

    @Override
    public ResponseEntity<RateProviderResponse> getRateProviderInfo(
            String code,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getRateProviderInfo(code));
    }
}
