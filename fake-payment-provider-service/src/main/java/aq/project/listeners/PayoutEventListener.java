package aq.project.listeners;

import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Payout;
import aq.project.mappers.PayoutMapper;
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
public class PayoutEventListener {

    private final PayoutMapper payoutMapper = PayoutMapper.INSTANCE;

    private final WebhookService webhookService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommitPayout(
            Payout payout
    ) {
        Status status = payout.getStatus();

        switch (status) {
            case PENDING -> {
                UpdateStatusOperationRequestDto dto = payoutMapper.toUpdateStatusOperationRequestDto(payout);

                dto.setStatus(Status.SUCCESS);

                webhookService.updateOperationStatus(dto);
            }
        }
    }
}