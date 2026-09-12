package aq.project.controllers.transaction;

import aq.project.controller.TransferTransactionRestControllerApi;
import aq.project.dto.TransactionStatus;
import aq.project.services.transaction.TransferTransactionService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransferTransactionRestController implements TransferTransactionRestControllerApi {

    private final TraceContext traceContext;

    private final TransferTransactionService transferTransactionService;

    @Override
    public ResponseEntity<TransactionStatus> getTransferTransactionStatus(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        return ResponseEntity.ok(transferTransactionService.getTransactionStatus(id));
    }
}
