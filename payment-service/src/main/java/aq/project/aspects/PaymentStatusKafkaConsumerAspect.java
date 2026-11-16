package aq.project.aspects;

import aq.project.kafka.events.PaymentStatusEvent;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Aspect
@Order(1)
@Validated
@Component
@RequiredArgsConstructor
public class PaymentStatusKafkaConsumerAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.kafka.PaymentStatusKafkaConsumer.onPaymentStatusEvent(..)) " +
            "&& args(paymentStatusEvent, acknowledgment)")
    public void onPaymentStatusEvent(
            ProceedingJoinPoint pjp,
            @NotNull @Valid PaymentStatusEvent paymentStatusEvent,
            Acknowledgment acknowledgment
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "on-payment-status-event";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to update a status of payment with id: [%s]",
                paymentStatusEvent.paymentId());

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