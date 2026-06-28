package aq.project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rateServiceClient", url = "${application.client.frankfurter.base-url}")
public interface FrankfurterRateProviderClient {

    @GetMapping("${application.client.frankfurter.endpoint.providers}")
    String getProviders();

    @GetMapping("${application.client.frankfurter.endpoint.rates}")
    String getRates(@RequestParam("base") String base, @RequestParam("expand") String expand);

    @GetMapping("${application.client.frankfurter.endpoint.rate}")
    String getRate(@PathVariable("base") String base, @PathVariable("quote") String quote);

    @GetMapping("${application.client.frankfurter.endpoint.currencies}")
    String getCurrencies();
}
