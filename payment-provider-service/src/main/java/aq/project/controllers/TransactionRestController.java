package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.CancelTransactionDto;
import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionResponseDto;
import aq.project.dto.TransactionStatus;
import aq.project.services.TransactionService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Void> cancelTransaction(
            String authorization,
            CancelTransactionDto cancelTransactionDto,
            String xTraceId
    ) {
        setUpTraceId(xTraceId);
        String merchantId = getMerchantId();
        String transactionId = cancelTransactionDto.getId();
        TransactionStatus transactionStatus = cancelTransactionDto.getStatus();
        transactionService.cancelTransaction(UUID.fromString(transactionId), merchantId, transactionStatus);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<TransactionResponseDto> createTransaction(
            String authorization,
            TransactionRequestDto transactionRequestDto,
            String xTraceId
    ) {
        setUpTraceId(xTraceId);
        String merchantId = getMerchantId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(transactionRequestDto, merchantId));
    }

    @Override
    public ResponseEntity<TransactionResponseDto> getTransactionInfo(
            String id,
            String authorization,
            String xTraceId
    ) {
        setUpTraceId(xTraceId);
        String merchantId = getMerchantId();
        return ResponseEntity.ok(transactionService.getTransactionInfo(UUID.fromString(id), merchantId));
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

    //    @Override
//    public ResponseEntity<TransactionResponseDto> createTransaction(
//            String authorization,
//            TransactionRequestDto transactionRequestDto,
//            String xTraceId
//    ) {
//        setUpTraceId(xTraceId);
//        String merchantId = getMerchantId();
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(transactionService.createTransaction(transactionRequestDto, merchantId));
//    }
//
//    @Override
//    public ResponseEntity<TransactionResponseDto> getTransactionInfo(
//            String transactionId,
//            String authorization,
//            String xTraceId
//    ) {
//        setUpTraceId(xTraceId);
//        String merchantId = getMerchantId();
//        return ResponseEntity.ok(transactionService.getTransactionInfo(UUID.fromString(transactionId), merchantId));
//    }
//
//    @Override
//    public ResponseEntity<List<TransactionResponseDto>> getTransactionList(
//            OffsetDateTime startDate,
//            OffsetDateTime endDate,
//            String authorization,
//            String xTraceId
//    ) {
//        setUpTraceId(xTraceId);
//        String merchantId = getMerchantId();
//        return ResponseEntity.ok(transactionService.getTransactionList(startDate, endDate, merchantId));
//    }
//
//    @Override
//    public ResponseEntity<Void> cancelTransaction(
//            String authorization,
//            CancelTransactionDto cancelTransactionDto,
//            String xTraceId
//    ) {
//        setUpTraceId(xTraceId);
//        String merchantId = getMerchantId();
//        String transactionId = cancelTransactionDto.getId();
//        TransactionStatus transactionStatus = cancelTransactionDto.getStatus();
//        transactionService.cancelTransaction(UUID.fromString(transactionId), merchantId, transactionStatus);
//        return ResponseEntity.ok().build();
//    }

    private void setUpTraceId(String xTraceId) {
        if(xTraceId != null && !xTraceId.trim().isEmpty())
            traceContext.setTraceId(xTraceId);
    }

    private String getMerchantId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
