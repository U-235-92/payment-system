package aq.project.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface FrankfurterRateProviderClient {

    @GetExchange(
            value = "${application.client.frankfurter.endpoint.providers}"
    )
    String getProviders();

    @GetExchange(
            value = "${application.client.frankfurter.endpoint.rates}"
    )
    String getRates(
            @RequestParam("base") String base,
            @RequestParam("expand") String expand
    );

    @GetExchange(
            value = "${application.client.frankfurter.endpoint.rate}/{base}/{quote}"
    )
    String getRate(
            @PathVariable("base") String base,
            @PathVariable("quote") String quote
    );

    @GetExchange(
            value = "${application.client.frankfurter.endpoint.currencies}"
    )
    String getCurrencies();
}
