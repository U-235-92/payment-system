package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import aq.project.util.mappers.TransactionRequestMapper;
import aq.project.messages.TransactionRequest;
import aq.project.services.TransactionService;
import aq.project.util.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    private final TransactionRequestMapper transactionRequestMapper;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<String> sendTransactionRequest(String xTraceId, TransactionRequestDTO dto) {
        traceContext.setTraceId(xTraceId);
        TransactionRequest transactionRequest = transactionRequestMapper.toTransactionRequest(dto);
        String transactionId = UUID.randomUUID().toString();
        transactionRequest.setTransactionId(transactionId);
        transactionRequest.setTransactionStatus(TransactionStatus.PENDING);
        return ResponseEntity.ok(transactionService.sendTransactionRequest(transactionRequest));
    }

    @Override
    public ResponseEntity<TransactionStatus> getTransactionStatus(String transactionId, String xTraceId) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(transactionService.getTransactionStatus(transactionId));
    }
}
