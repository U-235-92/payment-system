package aq.project.controllers;

import aq.project.controller.WebhookRestControllerApi;
import aq.project.dto.TransactionStatusDto;
import aq.project.services.WebhookService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebhookRestController implements WebhookRestControllerApi {

    private final TraceContext traceContext;

    private final WebhookService webhookService;

    @Override
    public ResponseEntity<Void> updateTransactionStatus(
            String authorization,
            TransactionStatusDto transactionStatusDto,
            String xTraceId
    ) {
        setUpTraceId(xTraceId);
        webhookService.updateTransactionStatus(transactionStatusDto);
        return ResponseEntity.ok().build();
    }

    private void setUpTraceId(String xTraceId) {
        if(xTraceId != null && !xTraceId.trim().isEmpty())
            traceContext.setTraceId(xTraceId);
    }
}
