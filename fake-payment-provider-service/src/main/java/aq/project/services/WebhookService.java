package aq.project.services;

import aq.project.client.WebhookClientApi;
import aq.project.dto.OperationType;
import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Webhook;
import aq.project.repositories.WebhookRepository;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookClientApi webhookClientApi;

    private final WebhookRepository webhookRepository;

    private final Fallback fallback;

    @Retry(
            name = "update-operation-status-retry",
            fallbackMethod = "updateOperationStatusRetryFallback"
    )
    @Transactional
    public void updateOperationStatus(
            UpdateStatusOperationRequestDto updateStatusOperationRequestDto
    ) {
        Long operationEntityId = updateStatusOperationRequestDto.getId();

        OperationType operationType = updateStatusOperationRequestDto.getOperationType();

        String notificationUrl = updateStatusOperationRequestDto.getNotificationUrl();

        ResponseEntity<Void> responseEntity;

        switch (operationType) {
            case TRANSACTION -> responseEntity = webhookClientApi
                    .confirmTransactionWebhook(updateStatusOperationRequestDto);

            case PAYOUT -> responseEntity = webhookClientApi
                    .confirmPayoutWebhook(updateStatusOperationRequestDto);

            default -> throw new IllegalStateException(
                    String.format("Unexpected operation type: [%s]", operationType));
        }

//        If was received 4xx/5xx status code response then error handler will handle that case (see ClientConfiguration)
        if(responseEntity.getStatusCode().is2xxSuccessful()) {
//            Save outbox entity to the final update of transaction/payout status.
//            See WebhookScheduler for details.updateOperationStatus(..).
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

        OperationType operationType = updateStatusOperationRequestDto.getOperationType();
        Status operationStatus = updateStatusOperationRequestDto.getStatus();
        Long operationId = updateStatusOperationRequestDto.getId();

        String message = String.format(
                "Exception occurred during update operation: [%s] with id: [%s] on status: [%s]. " +
                        "Exception message: %s",
                operationType, operationId, operationStatus, exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);
    }
}