package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    @Override
    public Mono<ResponseEntity<TransactionStatus>> getTransactionStatus(
            String transactionId,
            ServerWebExchange exchange
    ) {
        return transactionService.getTransactionStatus(transactionId)
                .flatMap(status -> Mono.just(ResponseEntity.ok(status)));
    }

    @Override
    public Mono<ResponseEntity<String>> doTransaction(Mono<TransactionRequestDto> transactionRequestDTO, ServerWebExchange exchange) {
        return transactionRequestDTO.flatMap(dto -> transactionService.doTransaction(dto)
                .flatMap(transactionId -> Mono.just(ResponseEntity.accepted().body(transactionId))));
    }
}
