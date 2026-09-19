package aq.project.controllers.transaction;

import aq.project.controller.WithdrawTransactionRestControllerApi;
import aq.project.dto.TransactionStatus;
import aq.project.services.transaction.WithdrawTransactionService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WithdrawTransactionRestController implements WithdrawTransactionRestControllerApi {

    private final TraceContext traceContext;

    private final WithdrawTransactionService withdrawTransactionService;

    @Override
    public ResponseEntity<TransactionStatus> getWithdrawTransactionStatus(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(withdrawTransactionService.getTransactionStatus(id));
    }
}
