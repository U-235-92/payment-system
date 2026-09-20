package aq.project.controllers;

import aq.project.controller.DepositTransactionRestControllerApi;
import aq.project.dto.TransactionServiceDepositTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.services.DepositTransactionService;
import aq.project.utils.mappers.DepositTransactionMapper;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DepositTransactionRestController implements DepositTransactionRestControllerApi {

    private final DepositTransactionMapper depositTransactionMapper = DepositTransactionMapper.INSTANCE;

    private final DepositTransactionService depositTransactionService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<UUID> createDepositTransaction(
            String xTraceId,
            TransactionServiceDepositTransactionRequestDto transactionServiceDepositTransactionRequestDto,
            String authorization
    ) {
        setUpTraceContext(xTraceId);

        TransactionServiceDepositTransaction transaction = depositTransactionMapper
                .toTransactionServiceDepositTransaction(transactionServiceDepositTransactionRequestDto);
        transaction.setStatus(TransactionStatus.PENDING);

        return ResponseEntity.ok(depositTransactionService.createTransaction(transaction));
    }

    @Override
    public ResponseEntity<TransactionStatus> getDepositTransactionStatus(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        setUpTraceContext(xTraceId);
        return ResponseEntity.ok(depositTransactionService.getTransactionStatus(id));
    }

    private void setUpTraceContext(String xTraceId) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);
    }
}
