package aq.project.services;

import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Webhook;
import aq.project.repositories.WebhookRepository;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.security.SecurityUtil;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import aq.project.webhook_service.WebhookApiClient;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    @Value("${app.security.access-token-key}")
    private String accessTokenKey;

    @Value("${app.security.access-token-hash-alg}")
    private String accessTokenHashAlg;

    private final WebhookApiClient webhookApiClient;

    private final WebhookRepository webhookRepository;

    private final TraceContext traceContext;

    private final Fallback fallback;

    @Retry(
            name = "update-operation-status-retry",
            fallbackMethod = "updateOperationStatusRetryFallback"
    )
    @Transactional
    public void updateOperationStatus(
            UpdateStatusOperationRequestDto updateStatusOperationRequestDto
    ) {
        UUID operationEntityId = updateStatusOperationRequestDto.getId();

        String operationType = updateStatusOperationRequestDto.getOperationType();

        String notificationUrl = updateStatusOperationRequestDto.getNotificationUrl();
        String traceId = traceContext.getTraceId() == null ? TraceFactory.genTraceId() : traceContext.getTraceId();
        String accessTokeHash = SecurityUtil.getHash(accessTokenKey, accessTokenHashAlg);

        log.debug(">>>>> Attempt to send request [webhook event] to [webhook-service]");

        ResponseEntity<Void> responseEntity = webhookApiClient
                .confirmOperationWebhook(accessTokeHash, accessTokenHashAlg, updateStatusOperationRequestDto, traceId);

        HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

        log.debug(">>>>> Received response on send request [webhook event] to [webhook-service] " +
                        "with status code: [{}]", httpStatusCode);

//        If was received 4xx/5xx status code response then error handler will handle that case.
//        See ClientConfiguration for details.
        if(httpStatusCode.is2xxSuccessful()) {
//            Save outbox entity to the final update of transaction/payout status.
//            See WebhookScheduler.updateOperationStatus(...) for details.
            Webhook webhook = Webhook.builder()
                    .operationType(operationType)
                    .entityId(operationEntityId)
                    .payload(null)
                    .receivedAt(OffsetDateTime.now())
                    .notificationUrl(notificationUrl)
                    .processed(false)
                    .build();

            webhookRepository.save(webhook);
        }
    }

    private void updateOperationStatusRetryFallback(
            UpdateStatusOperationRequestDto updateStatusOperationRequestDto,
            Exception exception
    ) {
        String action = "update-operation-status";

        String operationType = updateStatusOperationRequestDto.getOperationType();
        Status operationStatus = updateStatusOperationRequestDto.getStatus();
        UUID operationId = updateStatusOperationRequestDto.getId();

        String message = String.format(
                "Exception occurred during update operation: [%s] with id: [%s] on status: [%s]. " +
                        "Exception message: %s",
                operationType, operationId, operationStatus, exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);
    }
}