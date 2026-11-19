package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.*;
import aq.project.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    @Override
    public Mono<ResponseEntity<InitTransactionResponseDto>> initTransaction(
            PaymentType type,
            Mono<InitTransactionRequestDto> initTransactionRequestDto,
            String xTraceId,
            String authorization,
            ServerWebExchange exchange
    ) {
        return initTransactionRequestDto
                .flatMap(request -> transactionService.initTransaction(type, request))
                .flatMap(response -> Mono.just(ResponseEntity.ok().body(response)));
    }

    @Override
    public Mono<ResponseEntity<UUID>> confirmTransaction(
            PaymentType type,
            Mono<ConfirmTransactionRequestDto> confirmTransactionRequestDto,
            String xTraceId,
            String authorization,
            ServerWebExchange exchange
    ) {
        return confirmTransactionRequestDto
                .flatMap(request -> transactionService.confirmTransaction(type, request))
                .flatMap(response -> Mono.just(ResponseEntity.ok().body(response)));
    }

    @Override
    public Mono<ResponseEntity<TransactionStatus>> getTransactionStatus(
            UUID id,
            String xTraceId,
            String authorization,
            ServerWebExchange exchange
    ) {
        return transactionService.getTransactionStatus(id)
                .flatMap(response -> Mono.just(ResponseEntity.ok(response)));
    }

    @Override
    public Mono<ResponseEntity<Flux<TransactionInfoResponseDto>>> getTransactions(
            String xTraceId,
            String authorization,
            UUID userId,
            UUID walletId,
            PaymentType type,
            TransactionStatus status,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Integer page,
            Integer size,
            ServerWebExchange exchange
    ) {
        return transactionService
                .getTransactions(userId, walletId, type, status, dateFrom, dateTo, page, size)
                .flatMap(flux -> Mono.just(ResponseEntity.ok(flux)));
    }
}