package aq.project.clients;

import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public interface TransactionServiceWebClient {

    @GetExchange(
            value = "${application.transaction-service.endpoints.get-transaction-status}/{transactionId}"
    )
    Mono<ResponseEntity<TransactionStatus>> getTransactionStatus(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @PathVariable("transactionId") String transactionId
    );

    @PostExchange(
            value = "${application.transaction-service.endpoints.send-transaction-request}"
    )
    Mono<ResponseEntity<String>> doTransaction(
            @RequestHeader(name = AUTHORIZATION) String adminJwtAuthorizationValue,
            @RequestBody TransactionRequestDTO dto
    );
}
