package aq.project.aspects;

import aq.project.kafka.events.WebhookEvent;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Aspect
@Order(1)
@Validated
@Component
@RequiredArgsConstructor
public class WebhookEventKafkaConsumerAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${app.kafka.topics.payment-status-update-response.name}")
    private String withdrawalTransactionRequestTopic;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.kafka.WebhookEventKafkaConsumer.onWebhookEvent(..)) " +
            "&& args(webhookEvent)")
    public void onWebhookEvent(
            ProceedingJoinPoint pjp,
            @NotNull @Valid WebhookEvent webhookEvent
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "on-webhook-event";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received [%s] operation webhook event for transaction with id: [%s]. " +
                        "Source topic: [%s]",
                webhookEvent.status(), webhookEvent.transactionId(), withdrawalTransactionRequestTopic);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }
}