package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.*;
import aq.project.services.TransactionService;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
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
            String xTraceId,
            PaymentType type,
            InitTransactionRequestDto initTransactionRequestDto,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.ok(transactionService.initTransaction(initTransactionRequestDto));
    }

    @Override
    public ResponseEntity<UUID> confirmTransaction(
            String xTraceId,
            PaymentType type,
            ConfirmTransactionRequestDto confirmTransactionRequestDto,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.ok(transactionService.confirmTransaction(confirmTransactionRequestDto));
    }

    @Override
    public ResponseEntity<TransactionStatus> getTransactionStatus(
            String xTraceId,
            UUID id,
            String authorization
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        return ResponseEntity.ok(transactionService.getTransactionStatus(id));
    }
}
