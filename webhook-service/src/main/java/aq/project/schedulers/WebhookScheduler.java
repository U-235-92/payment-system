package aq.project.schedulers;

import aq.project.entities.PaymentProviderCallback;
import aq.project.kafka.WebhookKafkaProducer;
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

    private final WebhookKafkaProducer webhookKafkaProducer;

    @Scheduled(
            fixedRateString = "${app.scheduler.payment-provider-callback-scheduler}",
            timeUnit = TimeUnit.SECONDS
    )
    public void schedulePaymentProviderCallbacks() {
        List<PaymentProviderCallback> unprocessedCallbacks = paymentProviderCallbackRepository.findAllUnprocessed();

        for(PaymentProviderCallback unprocessedCallback : unprocessedCallbacks) {
            webhookKafkaProducer.sendWebhookEvent(unprocessedCallback);
        }
    }
}