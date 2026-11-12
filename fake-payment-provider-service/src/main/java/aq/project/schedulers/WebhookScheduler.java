package aq.project.schedulers;

import aq.project.dto.OperationType;
import aq.project.dto.Status;
import aq.project.entities.Payout;
import aq.project.entities.Transaction;
import aq.project.entities.Webhook;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.PayoutRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.repositories.WebhookRepository;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookScheduler {

    private final WebhookRepository webhookRepository;
    private final TransactionRepository transactionRepository;
    private final PayoutRepository payoutRepository;

    @Scheduled(
            fixedRateString = "${app.schedule.update-operation-status}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void updateOperationStatus() {
        String traceId = TraceFactory.genTraceId();

        log.info("[{}]: Schedule update operation status was started", traceId);

        List<Webhook> unprocessedWebhooks = webhookRepository.findAllUnprocessed();

        for(Webhook unprocessedWebhook : unprocessedWebhooks) {
            OperationType operationType = unprocessedWebhook.getOperationType();

            Long operationEntityId = unprocessedWebhook.getEntityId();

            try {
                switch (operationType) {
                    case PAYOUT -> {
                        log.info("[{}]: Attempt to update of payout status with payout id: [{}] on: [{}] status",
                                traceId, operationEntityId, Status.SUCCESS);

                        Payout payout = getPayout(operationEntityId);

                        Status status = payout.getStatus();

                        if(status == Status.PENDING) {
                            payout.complete();

                            log.info("[{}]: Update of payout status with payout id: [{}] completed",
                                    traceId, payout.getId());
                        } else {
                            onUnexpectedStatus(status);
                        }
                    }

                    case TRANSACTION -> {
                        log.info("[{}]: Attempt to update of transaction status with transaction id: [{}] on: [{}] status",
                                traceId, operationEntityId, Status.SUCCESS);

                        Transaction transaction = getTransaction(operationEntityId);

                        Status status = transaction.getStatus();

                        if(status == Status.PENDING) {
                            transaction.complete();

                            log.info("[{}]: Update of transaction status with transaction id: [{}] completed",
                                    traceId, transaction.getId());
                        } else {
                            onUnexpectedStatus(status);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[{}]: {} occurred while update [{}] status with id: [{}]. Exception message: {}",
                        traceId, e.getClass().getSimpleName(), operationType, operationEntityId, e.getMessage());
            } finally {
                unprocessedWebhook.processed();
            }
        }
        log.info("[{}]: Schedule update operation status was finished", traceId);
    }

    private Payout getPayout(Long id) {
        return payoutRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Payout with id: [%s] not found", id)));
    }

    private Transaction getTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id: [%s] not found", id)));
    }

    private void onUnexpectedStatus(Status status) {
        throw new IllegalStateException(
                String.format("Unexpected payout status: [%s]", status));
    }
}