package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.*;
import aq.project.services.TransactionService;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<InitTransactionResponseDto> initTransaction(
            PaymentType type,
            InitTransactionRequestDto initTransactionRequestDto,
            String xTraceId,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.ok(transactionService.initTransaction(initTransactionRequestDto));
    }

    @Override
    public ResponseEntity<UUID> confirmTransaction(
            PaymentType type,
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
    public ResponseEntity<Void> failTransaction(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        transactionService.failTransaction(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<Void> completeTransaction(
            UUID id,
            String xTraceId,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        transactionService.completeTransaction(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
