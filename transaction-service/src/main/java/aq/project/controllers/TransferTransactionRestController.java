package aq.project.controllers;

import aq.project.controller.TransferTransactionRestControllerApi;
import aq.project.dto.TransactionServiceTransferTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.services.TransferTransactionService;
import aq.project.utils.mappers.TransferTransactionMapper;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransferTransactionRestController implements TransferTransactionRestControllerApi {

    private final TransferTransactionMapper transferTransactionMapper = TransferTransactionMapper.INSTANCE;

    private final TransferTransactionService transferTransactionService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<UUID> createTransferTransaction(
            String xTraceId,
            TransactionServiceTransferTransactionRequestDto transactionServiceTransferTransactionRequestDto,
            String authorization
    ) {
        setUpTraceContext(xTraceId);

        TransactionServiceTransferTransaction transaction = transferTransactionMapper.toTransactionServiceTransferTransaction(transactionServiceTransferTransactionRequestDto);
        transaction.setStatus(TransactionStatus.PENDING);

        return ResponseEntity.ok(transferTransactionService.createTransaction(transaction));
    }

    @Override
    public ResponseEntity<TransactionStatus> getTransferTransactionStatus(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        setUpTraceContext(xTraceId);
        return ResponseEntity.ok(transferTransactionService.getTransactionStatus(id));
    }

    private void setUpTraceContext(String xTraceId) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);
    }
}
