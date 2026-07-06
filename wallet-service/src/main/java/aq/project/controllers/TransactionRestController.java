package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.TransactionStatus;
import aq.project.services.TransactionService;
import aq.project.util.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<TransactionStatus> getTransactionStatus(String id, String xTraceId) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(transactionService.getTransactionStatus(id));
    }
}
