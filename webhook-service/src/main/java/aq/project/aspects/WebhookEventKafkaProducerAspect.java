package aq.project.aspects;

import aq.project.dto.OperationType;
import aq.project.entities.PaymentProviderCallback;
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

import java.util.UUID;

@Aspect
@Order(1)
@Validated
@Component
@RequiredArgsConstructor
public class WebhookEventKafkaProducerAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.kafka.WebhookEventKafkaProducer.sendWebhookEvent(..)) " +
            "&& args(paymentProviderCallback)")
    public void sendWebhookEvent(
            ProceedingJoinPoint pjp,
            @NotNull @Valid PaymentProviderCallback paymentProviderCallback
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "send-webhook-event";
        String tracerName = serviceName + "." + actionName + "-tracer";

        UUID transactionId = paymentProviderCallback.getTransactionId();

        OperationType operationType = paymentProviderCallback.getOperationType();

        String logOnBeginning = String.format(
                "Received request to send webhook event for: [%s] with transaction id: [%s] to Kafka",
                operationType, transactionId);

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