package aq.project.schedulers;

import aq.project.entities.PaymentProviderCallback;
import aq.project.kafka.WebhookEventKafkaProducer;
import aq.project.repositories.PaymentProviderCallbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookScheduler {

    private final PaymentProviderCallbackRepository paymentProviderCallbackRepository;

    private final WebhookEventKafkaProducer webhookEventKafkaProducer;

    @Scheduled(
            fixedRateString = "${app.scheduler.payment-provider-callback-scheduler}",
            timeUnit = TimeUnit.SECONDS
    )
    public void schedulePaymentProviderCallbacks() {
        List<PaymentProviderCallback> unprocessedCallbacks = paymentProviderCallbackRepository.findAllUnprocessed();

        try {
            for(PaymentProviderCallback unprocessedCallback : unprocessedCallbacks) {
                log.debug(">>>>> Schedule unprocessed callback for transaction with id {}",
                        unprocessedCallback.getTransactionId());

                webhookEventKafkaProducer.sendWebhookEvent(unprocessedCallback);

                log.debug(">>>>> Success schedule unprocessed callback for transaction with id {}",
                        unprocessedCallback.getTransactionId());
            }
        } catch (Exception e) {
            log.error("Error occurred while schedule publishing payment provider callback. Exception message: {}",
                    e.getMessage());
        }
    }
}