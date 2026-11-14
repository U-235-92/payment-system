package aq.project.aspects;

import aq.project.entities.Transaction;
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
public class KafkaEventProducerAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("{app.kafka.topics.deposit_transaction_request.name}")
    private String depositTransactionRequestTopic;
    @Value("{app.kafka.topics.withdrawal_transaction_request.name}")
    private String withdrawalTransactionRequestTopic;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.kafka.KafkaEventProducer.sendDepositTransactionRequestEvent(..)) " +
            "&& args(transaction)")
    public void sendDepositTransactionRequestEvent(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Transaction transaction
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "send-deposit-transaction-request-event";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Sending [deposit] transaction with transaction id: [%s] " +
                "request event to kafka-topic: [%s]",
                transaction.getId(), depositTransactionRequestTopic);

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

    @Around("execution(* aq.project.kafka.KafkaEventProducer.sendWithdrawalTransactionRequestEvent(..)) " +
            "&& args(transaction)")
    public void sendWithdrawalTransactionRequestEvent(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Transaction transaction
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "send-withdrawal-transaction-request-event";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Sending [withdrawal] transaction with transaction id: [%s] " +
                "request event to kafka-topic: [%s]",
                transaction.getId(), withdrawalTransactionRequestTopic);

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
