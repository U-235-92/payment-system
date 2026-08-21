package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.TransactionStatus;
import aq.project.services.TransactionService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<TransactionStatus> getTransactionStatus(
            String id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(transactionService.getTransactionStatus(id));
    }
}
