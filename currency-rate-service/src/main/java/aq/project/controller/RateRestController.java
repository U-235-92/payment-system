package aq.project.controller;

import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import aq.project.service.RateService;
import aq.project.util.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RateRestController implements RatesRestControllerApi {

    private final RateService rateService;
    private final TraceContext traceContext;

    @Override
    public ResponseEntity<List<CurrencyResponse>> getCurrencies(String xTraceId) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getCurrencies());
    }

    @Override
    public ResponseEntity<RateResponse> getRate(String from, String to, String xTraceId, String provider, LocalDate date) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getRate(from, to, provider, date));
    }

    @Override
    public ResponseEntity<List<RateProviderResponse>> getRateProviders(String xTraceId) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getRateProviders());
    }

    @Override
    public ResponseEntity<CurrencyResponse> getCurrencyInfo(String code, String xTraceId) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getCurrencyInfo(code));
    }

    @Override
    public ResponseEntity<RateProviderResponse> getRateProviderInfo(String code, String xTraceId) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(rateService.getRateProviderInfo(code));
    }
}
