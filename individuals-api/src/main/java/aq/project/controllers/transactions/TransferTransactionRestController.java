package aq.project.controllers.transactions;

import aq.project.controller.TransferTransactionRestControllerApi;
import aq.project.dto.IndividualsApiServiceTransferTransactionRequestDto;
import aq.project.services.transactions.TransferTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransferTransactionRestController implements TransferTransactionRestControllerApi {

    private final TransferTransactionService transferTransactionService;

    @Override
    public Mono<ResponseEntity<UUID>> createTransferTransaction(
            Mono<IndividualsApiServiceTransferTransactionRequestDto> createTransferTransactionRequest,
            ServerWebExchange exchange
    ) {
        return createTransferTransactionRequest.flatMap(transferTransactionService::createTransferTransaction)
                .map(uuid -> ResponseEntity.ok().body(uuid));
    }

    @Override
    public Mono<ResponseEntity<String>> getTransferTransactionStatus(
            UUID transactionId,
            ServerWebExchange exchange
    ) {
        return transferTransactionService.getTransferTransactionStatus(transactionId)
                .map(transactionStatus -> ResponseEntity.ok().body(transactionStatus.toString()));
    }
}
