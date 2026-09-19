package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.messages.TransactionRequest;
import aq.project.services.TransactionService;
import aq.project.utils.mappers.TransactionRequestMapper;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;

    private final TransactionService transactionService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<String> sendTransactionRequest(
            String xTraceId,
            TransactionRequestDto dto,
            String authorization
    ) {
        setUpTraceContext(xTraceId);

        TransactionRequest transactionRequest = transactionRequestMapper.toTransactionRequest(dto);

        transactionService.sendTransactionRequest(transactionRequest);

        return ResponseEntity.ok(transactionRequest.getTransactionId());
    }

    @Override
    public ResponseEntity<TransactionStatus> getTransactionStatus(
            String transactionId,
            String xTraceId,
            String authorization
    ) {
        setUpTraceContext(xTraceId);

        return ResponseEntity.ok(transactionService.getTransactionStatus(transactionId));
    }

    private void setUpTraceContext(String xTraceId) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);
    }
}
