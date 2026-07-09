package aq.project.clients;

import aq.project.dto.TransactionStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

import static aq.project.util.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public interface WalletServiceRestClient {

    @GetExchange(
            value = "${service.wallet-service.endpoints.get-transaction-status}/{transactionId}"
    )
    TransactionStatus getTransactionStatus(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizeHeaderValue,
            @RequestHeader(name = X_TRACE_ID_HEADER) String xTraceId,
            @PathVariable("transactionId") String transactionId
    );
}
