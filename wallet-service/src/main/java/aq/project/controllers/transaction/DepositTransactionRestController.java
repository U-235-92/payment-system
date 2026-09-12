package aq.project.controllers.transaction;

import aq.project.controller.DepositTransactionRestControllerApi;
import aq.project.dto.TransactionStatus;
import aq.project.services.transaction.DepositTransactionService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DepositTransactionRestController implements DepositTransactionRestControllerApi {

    private final TraceContext traceContext;

    private final DepositTransactionService depositTransactionService;

    @Override
    public ResponseEntity<TransactionStatus> getDepositTransactionStatus(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(depositTransactionService.getTransactionStatus(id));
    }
}
