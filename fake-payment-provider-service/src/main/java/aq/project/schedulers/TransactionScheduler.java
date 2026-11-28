package aq.project.schedulers;

import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Transaction;
import aq.project.mappers.TransactionMapper;
import aq.project.repositories.TransactionRepository;
import aq.project.services.WebhookService;
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
public class TransactionScheduler {

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final WebhookService webhookService;

    private final TransactionRepository transactionRepository;

    @Scheduled(
            fixedRateString = "${app.schedule.update-transaction-status}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void updateTransactionStatus() {
        String traceId = TraceFactory.genTraceId();

        try {
            Status targetStatus = Status.PENDING;

            List<Transaction> pendingTransactions = transactionRepository.findAllByStatus(targetStatus);

            for (Transaction pendingTransaction : pendingTransactions) {
                try {
                    UpdateStatusOperationRequestDto dto = transactionMapper
                            .toUpdateStatusOperationRequestDto(pendingTransaction);

                    dto.setStatus(Status.SUCCESS);

                    log.debug(">>>>> Schedule update transaction status on: [{}] for transaction with id: [{}]",
                            dto.getStatus(), dto.getTransactionId());

//                    Request to update transaction on success status
                    webhookService.updateOperationStatus(dto);

//                    Before status of transaction is updated mark one as processing.
                    pendingTransaction.processing();

                    log.debug(">>>>> Success schedule update transaction status on: [{}] for transaction with id: [{}]",
                            dto.getStatus(), dto.getTransactionId());

                } catch (Exception e) {
                    log.error("[{}]: {} occurred while update status of transaction with id: [{}]. Exception message: {}",
                            traceId, e.getClass().getSimpleName(), pendingTransaction.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[{}]: {} occurred while prepare to update statuses of transactions. Exception message: {}",
                    traceId, e.getClass().getSimpleName(), e.getMessage());
        }
    }
}