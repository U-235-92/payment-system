package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import aq.project.mappers.TransactionRequestMapper;
import aq.project.messages.TransactionRequest;
import aq.project.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    private final TransactionRequestMapper transactionRequestMapper;

    public ResponseEntity<String> sendTransactionRequest(TransactionRequestDTO dto) {
        TransactionRequest transactionRequest = transactionRequestMapper.toTransactionRequest(dto);
        String transactionId = UUID.randomUUID().toString();
        transactionRequest.setTransactionId(transactionId);
        transactionRequest.setTransactionStatus(TransactionStatus.PENDING);
        return ResponseEntity.ok(transactionService.sendTransactionRequest(transactionRequest));
    }

    public ResponseEntity<TransactionStatus> getTransactionStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionStatus(transactionId));
    }
}
