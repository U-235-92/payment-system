package aq.project.schedulers;

import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Payout;
import aq.project.mappers.PayoutMapper;
import aq.project.repositories.PayoutRepository;
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
public class PayoutScheduler {

    private final PayoutMapper payoutMapper = PayoutMapper.INSTANCE;

    private final WebhookService webhookService;

    private final PayoutRepository payoutRepository;

    @Scheduled(
            fixedRateString = "${app.schedule.update-payout-status}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void updatePayoutStatus() {
        String traceId = TraceFactory.genTraceId();

        try {
            Status targetStatus = Status.PENDING;

            List<Payout> pendingPayouts = payoutRepository.findAllByStatus(targetStatus);

            for (Payout pendingPayout : pendingPayouts) {
                try {
                    UpdateStatusOperationRequestDto dto = payoutMapper
                            .toUpdateStatusOperationRequestDto(pendingPayout);

                    dto.setStatus(Status.SUCCESS);

//                    Request to update payment on success status
                    webhookService.updateOperationStatus(dto);

//                    Before status of payment is updated mark one as processing.
                    pendingPayout.processing();
                } catch (Exception e) {
                    log.error("[{}]: {} occurred while update status of payout with id: [{}]. Exception message: {}",
                            traceId, e.getClass().getSimpleName(), pendingPayout.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[{}]: {} occurred while prepare to update statuses of payouts. Exception message: {}",
                    traceId, e.getClass().getSimpleName(), e.getMessage());
        }
    }
}