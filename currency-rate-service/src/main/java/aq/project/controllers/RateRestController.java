package aq.project.controllers;

import aq.project.controller.RateRestControllerApi;
import aq.project.dto.CurrencyResponseDto;
import aq.project.dto.RateProviderResponseDto;
import aq.project.dto.RateResponseDto;
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
    public ResponseEntity<List<CurrencyResponseDto>> getCurrencies(
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getCurrencies());
    }

    @Override
    public ResponseEntity<RateResponseDto> getRate(
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
    public ResponseEntity<List<RateProviderResponseDto>> getRateProviders(
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getRateProviders());
    }

    @Override
    public ResponseEntity<CurrencyResponseDto> getCurrencyInfo(
            String code,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getCurrencyInfo(code));
    }

    @Override
    public ResponseEntity<RateProviderResponseDto> getRateProviderInfo(
            String code,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getRateProviderInfo(code));
    }
}
