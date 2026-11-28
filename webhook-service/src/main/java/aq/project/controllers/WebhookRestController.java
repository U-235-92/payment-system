package aq.project.controllers;

import aq.project.controller.WebhookRestControllerApi;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.services.WebhookService;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebhookRestController implements WebhookRestControllerApi {

    private final WebhookService webhookService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<Void> confirmOperationWebhook(
            String xAccessTokenHash,
            String xAccessTokenEncoding,
            UpdateStatusOperationRequestDto updateStatusOperationRequestDto,
            String xTraceId
    ) {
        TraceFactory.setUpTraceId(traceContext, xTraceId);

        webhookService.confirmOperation(updateStatusOperationRequestDto);

        return ResponseEntity.ok().build();
    }
}