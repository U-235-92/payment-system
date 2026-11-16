package aq.project.listeners;

import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Transaction;
import aq.project.mappers.TransactionMapper;
import aq.project.services.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final WebhookService webhookService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommitTransaction(
            Transaction transaction
    ) {
        Status status = transaction.getStatus();

        switch (status) {
            case PENDING -> {
                UpdateStatusOperationRequestDto dto = transactionMapper.toUpdateStatusOperationRequestDto(transaction);

                dto.setStatus(Status.SUCCESS);

                webhookService.updateOperationStatus(dto);
            }
        }
    }
}