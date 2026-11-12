package aq.project.services;

import aq.project.client.WebhookClientApi;
import aq.project.dto.OperationType;
import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Payout;
import aq.project.entities.Transaction;
import aq.project.entities.Webhook;
import aq.project.exceptions.FallbackOperationException;
import aq.project.mappers.PayoutMapper;
import aq.project.mappers.TransactionMapper;
import aq.project.repositories.PayoutRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.repositories.WebhookRepository;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PayoutMapper payoutMapper = PayoutMapper.INSTANCE;
    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final WebhookClientApi webhookClientApi;

    private final TransactionRepository transactionRepository;
    private final PayoutRepository payoutRepository;
    private final WebhookRepository webhookRepository;

    private final Fallback fallback;

    @Async
    @Retry(
            name = "complete-transaction-retry",
            fallbackMethod = "completeTransactionAsyncFallback"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void completeTransactionAsync(Transaction transaction) {
        if(transaction.getStatus() == Status.PENDING) {
            UpdateStatusOperationRequestDto dto = transactionMapper.toUpdateStatusOperationRequestDto(transaction);

            dto.setStatus(Status.SUCCESS);

            updateOperationStatus(dto);
        }
    }

    private void completeTransactionAsyncFallback(Transaction transaction, Exception exception) {
        String action = "complete-transaction-async";

        String message = String.format(
                "Exception occurred during update transaction status on: [%s] for transaction with id: [%s]. " +
                        "Exception message: %s", Status.SUCCESS, transaction.getId(), exception.getMessage());

        transaction.fail();

        transactionRepository.save(transaction);

        try {
            fallback.handleRetryFallback(action, message, exception);
        } catch (FallbackOperationException e) {
//            Suppressed to commit fail of transaction without rollback
        }
    }

    @Async
    @Retry(
            name = "complete-payout-retry",
            fallbackMethod = "completePayoutAsyncFallback"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void completePayoutAsync(Payout payout) {
        if(payout.getStatus() == Status.PENDING) {
            UpdateStatusOperationRequestDto dto = payoutMapper.toUpdateStatusOperationRequestDto(payout);

            dto.setStatus(Status.SUCCESS);

            updateOperationStatus(dto);
        }
    }

    private void completePayoutAsyncFallback(Payout payout, Exception exception) {
        String action = "complete-payout-async";

        String message = String.format(
                "Exception occurred during update payout status on: [%s] for payout with id: [%s]. " +
                        "Exception message: %s", Status.SUCCESS, payout.getId(), exception.getMessage());

        payout.fail();

        payoutRepository.save(payout);

        try {
            fallback.handleRetryFallback(action, message, exception);
        } catch (FallbackOperationException e) {
//            Suppressed to commit fail of payout without rollback
        }
    }

    private void updateOperationStatus(UpdateStatusOperationRequestDto updateStatusOperationRequestDto) {
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
            Webhook webhook = Webhook.builder()
                    .operationType(operationType)
                    .entityId(operationEntityId)
                    .payload(null)
                    .receivedAt(OffsetDateTime.now())
                    .notificationUrl(notificationUrl)
                    .processed(false)
                    .build();

//            Save outbox entity. The final update of transaction/payout status will continue on WebhookScheduler side

            webhookRepository.save(webhook);
        }
    }
}
