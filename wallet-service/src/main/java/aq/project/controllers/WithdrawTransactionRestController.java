package aq.project.controllers;

import aq.project.controller.WithdrawTransactionRestControllerApi;
import aq.project.dto.TransactionStatus;
import aq.project.services.WithdrawTransactionService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WithdrawTransactionRestController implements WithdrawTransactionRestControllerApi {

    private final TraceContext traceContext;

    private final WithdrawTransactionService withdrawTransactionService;

    @Override
    public ResponseEntity<TransactionStatus> getWithdrawTransactionStatus(
            String id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(withdrawTransactionService.getTransactionStatus(id));
    }
}
