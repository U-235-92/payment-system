package aq.project.util.aspects;

import aq.project.dto.TransactionStatus;
import aq.project.exceptions.DuplicateTransactionException;
import aq.project.messages.TransactionRequest;
import aq.project.repositories.TransactionRepository;
import aq.project.util.telemetry.ServiceAspectHandler;
import aq.project.util.telemetry.TraceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import static aq.project.util.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static aq.project.util.constants.RequestPropertyKeys.*;
import static aq.project.util.constants.RequestPropertyKeys.SENDER_PERSON_ID;

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

    private final TransactionRepository transactionRepository;

    @Around("execution(* aq.project.services.TransactionService.handleTransactionRequest(..)) && args(consumerRecord)")
    public void aspectHandleTransactionRequest(
            ProceedingJoinPoint pjp,
            ConsumerRecord<String, TransactionRequest> consumerRecord
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = String.format("handle-%s-transaction-request", getTransactionType(consumerRecord));
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

    private String getTransactionType(ConsumerRecord<String, TransactionRequest> consumerRecord) {
        if(consumerRecord == null)
            throw new ConstraintViolationException("Received null Kafka consumer record", null);

        TransactionRequest transactionRequest = consumerRecord.value();
        if(transactionRequest == null)
            throw new ConstraintViolationException("Transaction request is null", null);

        Set<ConstraintViolation<TransactionRequest>> constraintViolations = validator.validate(transactionRequest);
        if(!constraintViolations.isEmpty())
            throw new ConstraintViolationException(constraintViolations);

        return transactionRequest.getOperationType().getValue().toLowerCase();
    }

    private Supplier<Void> checkConstraints(ConsumerRecord<String, TransactionRequest> consumerRecord) {
        TransactionRequest transactionRequest = consumerRecord.value();
        Headers headers = consumerRecord.headers();
        String operationType = transactionRequest.getOperationType().name().toLowerCase();
        String transactionId = transactionRequest.getTransactionId();
        checkUuidProperties(headers, RECIPIENT_WALLET_ID, transactionId);
        checkUuidProperties(headers, RECIPIENT_PERSON_ID, transactionId);
        checkUuidProperties(headers, SENDER_WALLET_ID, transactionId);
        checkUuidProperties(headers, SENDER_PERSON_ID, transactionId);
        checkIsPositiveAmount(transactionRequest.getAmount());
        if(transactionRepository.findById(transactionRequest.getTransactionId()).isPresent())
            throw new DuplicateTransactionException(String
                    .format("Attempt to handle duplicate of %s transaction with id: %s",
                            operationType, transactionId));
        return null;
    }

    private void checkUuidProperties(Headers headers, String header, String transactionId) {
        String uuid = getPropertyFromHeader(headers, header);
        if(uuid != null) {
            if(isInvalidUuid(uuid)) {
                String msg = String.format("Invalid %s UUID property found: %s in transaction request with transaction id [%s]",
                        header, uuid, transactionId);
                throw new ConstraintViolationException(msg, null);
            }
        }
    }

    private String getPropertyFromHeader(Headers headers, String header) {
        try {
            byte[] bytes = headers.headers(header).iterator().next().value();
            return new String(bytes);
        } catch(NoSuchElementException e) {}
        return null;
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

    private Supplier<Void> propagateTraceContext(ConsumerRecord<String, TransactionRequest> consumerRecord) {
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
        String msg = String.format("Transaction request hasn't got the header: [%s]", X_TRACE_ID_HEADER);
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
