package aq.project.aspects;

import aq.project.kafka.events.TransactionResponseEvent;
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
public class KafkaEventConsumerAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.kafka.KafkaEventConsumer.handleCompleteTransactionEvent(..)) " +
            "&& args(transactionResponseEvent)")
    public void handleCompleteTransactionEvent(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransactionResponseEvent transactionResponseEvent
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "handle-complete-transaction-event";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received [complete] response of handle [%s] transaction with id: [%s]",
                transactionResponseEvent.paymentType(), transactionResponseEvent.transactionId());

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

    @Around("execution(* aq.project.kafka.KafkaEventConsumer.handleFailTransactionEvent(..)) " +
            "&& args(transactionResponseEvent)")
    public void handleFailTransactionEvent(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransactionResponseEvent transactionResponseEvent
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "handle-fail-transaction-event";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received [fail] response of handle [%s] transaction with id: [%s]",
                transactionResponseEvent.paymentType(), transactionResponseEvent.transactionId());

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
