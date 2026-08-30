package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.TransactionResponseDto;
import aq.project.services.TransactionService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionRestController implements TransactionRestControllerApi {

    private final TraceContext traceContext;

    private final TransactionService transactionService;

    @Override
    public ResponseEntity<TransactionResponseDto> getTransactionInfo(
            UUID id,
            String authorization,
            String xTraceId
    ) {
        setUpTraceId(xTraceId);
        String merchantId = getMerchantId();
        return ResponseEntity.ok(transactionService.getTransactionInfo(id, merchantId));
    }

    @Override
    public ResponseEntity<List<TransactionResponseDto>> getTransactionList(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String authorization,
            String xTraceId
    ) {
        setUpTraceId(xTraceId);
        String merchantId = getMerchantId();
        return ResponseEntity.ok(transactionService.getTransactionList(startDate, endDate, merchantId));
    }

    private void setUpTraceId(String xTraceId) {
        if(xTraceId != null && !xTraceId.trim().isEmpty())
            traceContext.setTraceId(xTraceId);
    }

    private String getMerchantId() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}
