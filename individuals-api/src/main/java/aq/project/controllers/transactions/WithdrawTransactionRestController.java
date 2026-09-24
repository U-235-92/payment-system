package aq.project.controllers.transactions;

import aq.project.controller.WithdrawTransactionRestControllerApi;
import aq.project.dto.IndividualsApiServiceWithdrawTransactionRequestDto;
import aq.project.services.transactions.WithdrawTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WithdrawTransactionRestController implements WithdrawTransactionRestControllerApi {

    private final WithdrawTransactionService withdrawTransactionService;

    @Override
    public Mono<ResponseEntity<UUID>> createWithdrawTransaction(
            Mono<IndividualsApiServiceWithdrawTransactionRequestDto> createWithdrawTransactionRequest,
            ServerWebExchange exchange
    ) {
        return createWithdrawTransactionRequest.flatMap(withdrawTransactionService::createWithdrawTransaction)
                .map(uuid -> ResponseEntity.ok().body(uuid));
    }

    @Override
    public Mono<ResponseEntity<String>> getWithdrawTransactionStatus(
            UUID transactionId,
            ServerWebExchange exchange
    ) {
        return withdrawTransactionService.getWithdrawTransactionStatus(transactionId)
                .map(transactionStatus -> ResponseEntity.ok().body(transactionStatus.toString()));
    }
}
