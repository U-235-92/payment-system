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
public class TransactionEventKafkaProducerAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${app.kafka.topics.deposit-transaction-request.name}")
    private String depositTransactionRequestTopic;
    @Value("${app.kafka.topics.withdrawal-transaction-request.name}")
    private String withdrawalTransactionRequestTopic;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.kafka.TransactionEventKafkaProducer.publishTransactionEvent(..)) " +
            "&& args(transaction)")
    public void publishTransactionEvent(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Transaction transaction
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "publish-transaction-event";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Publish [%s] transaction with transaction id: [%s] " +
                "request event to kafka-topic: [%s]",
                transaction.getPaymentType(), transaction.getId(), depositTransactionRequestTopic);

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