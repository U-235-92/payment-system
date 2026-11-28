package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.*;
import aq.project.services.TransactionService;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<InitTransactionResponseDto> initTransaction(
            InitTransactionRequestDto initTransactionRequestDto,
            String xTraceId,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.ok(transactionService.initTransaction(initTransactionRequestDto));
    }

    @Override
    public ResponseEntity<UUID> confirmTransaction(
            ConfirmTransactionRequestDto confirmTransactionRequestDto,
            String xTraceId,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.ok(transactionService.confirmTransaction(confirmTransactionRequestDto));
    }

    @Override
    public ResponseEntity<TransactionStatus> getTransactionStatus(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.ok(transactionService.getTransactionStatus(id));
    }

    @Override
    public ResponseEntity<List<TransactionInfoResponseDto>> getTransactions(
            String xTraceId,
            String authorization,
            UUID userId,
            UUID walletId,
            PaymentType type,
            TransactionStatus status,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Integer page,
            Integer size
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.ok(transactionService
                .getTransactions(userId, walletId, type, status, dateFrom, dateTo, page, size));
    }

    @Override
    public ResponseEntity<Void> failTransaction(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        transactionService.failTransaction(id);

        return ResponseEntity.ok().build();
    }
}