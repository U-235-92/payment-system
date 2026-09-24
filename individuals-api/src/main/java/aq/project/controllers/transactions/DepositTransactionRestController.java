package aq.project.controllers.transactions;

import aq.project.controller.DepositTransactionRestControllerApi;
import aq.project.dto.IndividualsApiServiceDepositTransactionRequestDto;
import aq.project.services.transactions.DepositTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DepositTransactionRestController implements DepositTransactionRestControllerApi {

    private final DepositTransactionService depositTransactionService;

    @Override
    public Mono<ResponseEntity<UUID>> createDepositTransaction(
            Mono<IndividualsApiServiceDepositTransactionRequestDto> createDepositTransactionRequest,
            ServerWebExchange exchange
    ) {
        return createDepositTransactionRequest.flatMap(depositTransactionService::createDepositTransaction)
                .map(uuid -> ResponseEntity.ok().body(uuid));
    }

    @Override
    public Mono<ResponseEntity<String>> getDepositTransactionStatus(
            UUID transactionId,
            ServerWebExchange exchange
    ) {
        return depositTransactionService.getDepositTransactionStatus(transactionId)
                .map(transactionStatus -> ResponseEntity.ok().body(transactionStatus.toString()));
    }
}
