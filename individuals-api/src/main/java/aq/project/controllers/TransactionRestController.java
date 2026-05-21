package aq.project.controllers;

import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import aq.project.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    @GetMapping("/status/{transactionId}")
    public Mono<ResponseEntity<TransactionStatus>> getTransactionStatus(@PathVariable String transactionId) {
        return transactionService.getTransactionStatus(transactionId)
                .flatMap(status -> Mono.just(ResponseEntity.ok(status)));
    }

    @PostMapping("/do-transaction")
    public Mono<ResponseEntity<String>> doTransaction(@RequestBody TransactionRequestDTO dto) {
        return transactionService.doTransaction(dto)
                .flatMap(id -> Mono.just(ResponseEntity.accepted().body(id)));
    }
}
