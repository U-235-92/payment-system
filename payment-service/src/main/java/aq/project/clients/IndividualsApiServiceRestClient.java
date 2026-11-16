package aq.project.clients;

import aq.project.constants.ServiceConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.PostExchange;

import java.util.UUID;

public interface IndividualsApiServiceRestClient {

    @PostExchange(
            value = "${app.service.individuals-api.endpoints.complete-transaction}/{id}"
    )
    void completeTransaction(
            @PathVariable(name = "id") UUID transactionId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(name = ServiceConstants.X_TRACE_ID_HEADER) String traceId
    );

    @PostExchange(
            value = "${app.service.individuals-api.endpoints.fail-transaction}/{id}"
    )
    void failTransaction(
            @PathVariable(name = "id") UUID transactionId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(name = ServiceConstants.X_TRACE_ID_HEADER) String traceId
    );
}
