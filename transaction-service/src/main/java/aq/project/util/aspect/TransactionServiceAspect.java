package aq.project.util.aspect;

import aq.project.dto.TransactionStatus;
import aq.project.messages.TransactionRequest;
import aq.project.messages.TransactionResponse;
import aq.project.util.telemetry.ServiceAspectHandler;
import aq.project.util.telemetry.TraceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import static aq.project.util.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static aq.project.util.constants.RequestPropertyKeys.*;

@Slf4j
@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    private final TraceContext traceContext;

    private final Validator validator;

    @Around("execution(* aq.project.services.TransactionService.sendTransactionRequest(..)) && args(transactionRequest)")
    public String aspectSendTransactionRequest(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransactionRequest transactionRequest
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "send-transaction-request";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String operation = transactionRequest.getOperationType().toString().toLowerCase();
        String transactionId = transactionRequest.getTransactionId();
        String preMainLogicLogMessage = String.format("Received request to handle [%s] transaction with id: %s",
                operation, transactionId);
        String postSuccessMainLogicCallLogMessage = String.format("Success request to handle [%s] transaction with id: %s",
                operation, transactionId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle [%s] transaction request with id: %s",
                operation, transactionId);
//        Handler logic call
        return serviceAspectHandler.handle(
                String.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                checkConstraints(transactionRequest),
                null,
                null
        );
    }

    private Supplier<Void> checkConstraints(TransactionRequest transactionRequest) {
        return () -> {
            checkUuidProperties(transactionRequest, RECIPIENT_WALLET_ID);
            checkUuidProperties(transactionRequest, RECIPIENT_PERSON_ID);
            checkUuidProperties(transactionRequest, SENDER_WALLET_ID);
            checkUuidProperties(transactionRequest, SENDER_PERSON_ID);
            checkIsPositiveAmount(transactionRequest.getAmount());
            return null;
        };
    }

    private void checkUuidProperties(TransactionRequest transactionRequest, String key) {
        if(!transactionRequest.isPropertyNull(key)) {
            String uuid = transactionRequest.getProperty(key);
            if(isInvalidUuid(uuid)) {
                String msg = String.format("Invalid %s UUID property found: %s", key, uuid);
                throw new ConstraintViolationException(msg, null);
            }
        }
    }

    private boolean isInvalidUuid(String uuid) {
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        return !matcher.find();
    }

    private void checkIsPositiveAmount(BigDecimal amount) {
        if(amount.compareTo(new BigDecimal(0)) <= 0) {
            String msg = "The amount of the transaction must be greater than zero";
            throw new ConstraintViolationException(msg, null);
        }
    }

    @Around("execution(* aq.project.services.TransactionService.handleTransactionResponse(..)) && args(consumerRecord)")
    public void aspectHandleTransactionResponse(
            ProceedingJoinPoint pjp,
            ConsumerRecord<String, TransactionResponse> consumerRecord
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "handle-transaction-response";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received record from Kafka topic: %s, partition: %s, offset: %s",
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
        String postSuccessMainLogicCallLogMessage = String.format("Success handle record from Kafka topic: %s, partition: %s, offset: %s",
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle record from Kafka topic: %s, partition: %s, offset: %s",
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
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
                propagateTraceContext(consumerRecord),
                null
        );
    }

    private Supplier<Void> checkConstraints(ConsumerRecord<String, TransactionResponse> consumerRecord) {
        return () -> {
            if(consumerRecord == null) {
                throw new ConstraintViolationException("Received null Kafka consumer record", null);
            }
            TransactionResponse transactionResponse = consumerRecord.value();
            if(transactionResponse == null) {
                throw new ConstraintViolationException("Transaction response is null", null);
            }
            Set<ConstraintViolation<TransactionResponse>> constraintViolations = validator.validate(transactionResponse);
            if(!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
            return null;
        };
    }

    private Supplier<Void> propagateTraceContext(ConsumerRecord<String, TransactionResponse> consumerRecord) {
        return () -> {
            traceContext.setTraceId(getTraceIdFromConsumerRecordHeader(consumerRecord));
            return null;
        };
    }

    private String getTraceIdFromConsumerRecordHeader(ConsumerRecord<String, ?> consumerRecord) {
        for(Header header : consumerRecord.headers()) {
            if(header.key().equals(X_TRACE_ID_HEADER)) {
                return new String(header.value());
            }
        }
        String msg = String.format("Transaction response hasn't got the header: [%s]", X_TRACE_ID_HEADER);
        throw new IllegalStateException(msg);
    }

    @Around("execution(* aq.project.services.TransactionService.getTransactionStatus(..)) && args(transactionId)")
    public TransactionStatus aspectGetTransactionStatus(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String transactionId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transaction-status";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get transaction status with id: %s",
                transactionId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle of request of transaction status with id: %s",
                transactionId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during getting transaction status with id: %s",
                transactionId);
//        Handler logic call
        return serviceAspectHandler.handle(
                TransactionStatus.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }
}
