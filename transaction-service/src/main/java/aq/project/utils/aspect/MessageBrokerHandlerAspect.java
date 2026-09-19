package aq.project.utils.aspect;

import aq.project.entities.Transaction;
import aq.project.messages.TransactionResponse;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Set;
import java.util.function.Supplier;

@Slf4j
@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class MessageBrokerHandlerAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    private final Validator validator;

    @Around("execution(* aq.project.utils.handlers.MessageBrokerHandler.sendTransactionRequestToMessageBroker(..)) && args(transaction)")
    public void sendTransactionRequestToMessageBroker(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Transaction transaction
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "send-transaction-request-to-message-broker";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String operation = transaction.getOperationType().toString().toLowerCase();
        String transactionId = transaction.getId();
        String preMainLogicLogMessage = String.format("Received request to send [%s] transaction with id: [%s] to message broker",
                operation, transactionId);
        String postSuccessMainLogicCallLogMessage = String.format("Success request to send [%s] transaction with id: [%s] to message broker",
                operation, transactionId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during send [%s] transaction with id: [%s] to message broker",
                operation, transactionId);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null,
                true
        );
    }

    @Around("execution(* aq.project.utils.handlers.MessageBrokerHandler.processTransactionResponse(..)) && args(consumerRecord)")
    public void processTransactionResponse(
            ProceedingJoinPoint pjp,
            ConsumerRecord<String, TransactionResponse> consumerRecord
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "process-transaction-response";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to process transaction response from message broker";
        String postSuccessMainLogicCallLogMessage = "";
        String postFailureMainLogicCallLogMessage = "Error occurred during process transaction response from message broker";

        if(consumerRecord != null) {
            TransactionResponse transactionResponse = consumerRecord.value();
            if(transactionResponse != null) {
                String operation = transactionResponse.getOperationType().toString().toLowerCase();
                String transactionId = transactionResponse.getTransactionId();
                preMainLogicLogMessage = String.format("Received request to process [%s] transaction response with id: [%s] from message broker",
                        operation, transactionId);
                postSuccessMainLogicCallLogMessage = String.format("Success request to process [%s] transaction response with id: [%s] from message broker",
                        operation, transactionId);
                postFailureMainLogicCallLogMessage = String.format("Error occurred during process [%s] transaction response with id: [%s] from message broker",
                        operation, transactionId);
            }
        }

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                checkConstraints(consumerRecord),
                null,
                null,
                true
        );
    }

    private Supplier<Void> checkConstraints(ConsumerRecord<String, TransactionResponse> consumerRecord) {
        return () -> {
            if(consumerRecord == null)
                throw new ConstraintViolationException("Received null Kafka consumer record", null);

            TransactionResponse transactionResponse = consumerRecord.value();
            if(transactionResponse == null)
                throw new ConstraintViolationException("Transaction response is null", null);

            Set<ConstraintViolation<TransactionResponse>> constraintViolations = validator.validate(transactionResponse);
            if(!constraintViolations.isEmpty())
                throw new ConstraintViolationException(constraintViolations);

            return null;
        };
    }
}
