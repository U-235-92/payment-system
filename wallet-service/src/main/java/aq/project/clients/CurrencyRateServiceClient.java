package aq.project.clients;

import aq.project.dto.RateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "currencyRateServiceClient", url = "${application.services.currency-rate-service.base-url}")
public interface CurrencyRateServiceClient {

    @GetMapping("${application.services.currency-rate-service.endpoint.rate}")
    RateResponse getRates(
            @RequestParam("base") String base,
            @RequestParam("quote") String quote,
            @RequestHeader(name = "x-trace-id") String xTraceId
    );
}
