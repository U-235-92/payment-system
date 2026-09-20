package aq.project.controllers;

import aq.project.controller.WithdrawTransactionRestControllerApi;
import aq.project.dto.TransactionServiceWithdrawTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.services.WithdrawTransactionService;
import aq.project.utils.mappers.WithdrawTransactionMapper;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WithdrawTransactionRestController implements WithdrawTransactionRestControllerApi {

    private final WithdrawTransactionMapper withdrawTransactionMapper = WithdrawTransactionMapper.INSTANCE;

    private final WithdrawTransactionService withdrawTransactionService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<UUID> createWithdrawTransaction(
            String xTraceId,
            TransactionServiceWithdrawTransactionRequestDto transactionServiceWithdrawTransactionRequestDto,
            String authorization
    ) {
        setUpTraceContext(xTraceId);

        TransactionServiceWithdrawTransaction transaction = withdrawTransactionMapper.toTransactionServiceWithdrawTransaction(transactionServiceWithdrawTransactionRequestDto);
        transaction.setStatus(TransactionStatus.PENDING);

        return ResponseEntity.ok(withdrawTransactionService.createTransaction(transaction));
    }

    @Override
    public ResponseEntity<TransactionStatus> getWithdrawTransactionStatus(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        setUpTraceContext(xTraceId);
        return ResponseEntity.ok(withdrawTransactionService.getTransactionStatus(id));
    }

    private void setUpTraceContext(String xTraceId) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);
    }
}
