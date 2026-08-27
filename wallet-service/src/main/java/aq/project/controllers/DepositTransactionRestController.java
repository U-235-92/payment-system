package aq.project.controllers;

import aq.project.controller.DepositTransactionRestControllerApi;
import aq.project.dto.TransactionStatus;
import aq.project.services.DepositTransactionService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DepositTransactionRestController implements DepositTransactionRestControllerApi {

    private final TraceContext traceContext;

    private final DepositTransactionService depositTransactionService;

    @Override
    public ResponseEntity<TransactionStatus> getDepositTransactionStatus(
            String id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(depositTransactionService.getTransactionStatus(id));
    }
}
