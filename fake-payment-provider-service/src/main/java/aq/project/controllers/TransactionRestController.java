package aq.project.controllers;

import aq.project.controller.TransactionRestControllerApi;
import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.TransactionInfoResponseDto;
import aq.project.services.AuthenticationService;
import aq.project.services.TransactionService;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TransactionRestController implements TransactionRestControllerApi {

    private final TransactionService transactionService;

    private final AuthenticationService authenticationService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<TransactionInfoResponseDto> createTransaction(
            String authorization,
            CreateTransactionRequestDto createTransactionRequestDto,
            String xTraceId
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        Authentication authentication = authenticationService.getAuthentication();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(createTransactionRequestDto, authentication));
    }

    @Override
    public ResponseEntity<TransactionInfoResponseDto> getTransactionInfo(
            Long id,
            String authorization,
            String xTraceId
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        Authentication authentication = authenticationService.getAuthentication();

        return ResponseEntity.ok(transactionService.getTransactionInfo(id, authentication));
    }

    @Override
    public ResponseEntity<List<TransactionInfoResponseDto>> getTransactionList(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String authorization,
            String xTraceId
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        Authentication authentication = authenticationService.getAuthentication();

        return ResponseEntity.ok(transactionService.getTransactionList(startDate, endDate, authentication));
    }
}
