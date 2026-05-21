package aq.project.controllers;

import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import aq.project.mappers.TransactionRequestMapper;
import aq.project.messages.TransactionRequest;
import aq.project.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    private final TransactionRequestMapper transactionRequestMapper;

    @PostMapping("/send")
    public String sendTransactionRequest(@RequestBody TransactionRequestDTO dto) throws ExecutionException, InterruptedException {
        TransactionRequest transactionRequest = transactionRequestMapper.toTransactionRequest(dto);
        String transactionId = UUID.randomUUID().toString();
        transactionRequest.setTransactionId(transactionId);
        transactionRequest.setTransactionStatus(TransactionStatus.PENDING);
        return transactionService.sendTransactionRequest(transactionRequest);
    }

    @GetMapping("/status/{transactionId}")
    public TransactionStatus getTransactionStatus(@PathVariable String transactionId) {
        return transactionService.getTransactionStatus(transactionId);
    }
}
