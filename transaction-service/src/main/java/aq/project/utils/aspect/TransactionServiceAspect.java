package aq.project.utils.aspect;

import aq.project.dto.TransactionStatus;
import aq.project.messages.TransactionRequest;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import static aq.project.utils.constants.RequestPropertyKeys.*;

@Slf4j
@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TransactionService.sendTransactionRequest(..)) && args(transactionRequest)")
    public String sendTransactionRequest(
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
                null,
                true
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

    @Around("execution(* aq.project.services.TransactionService.getTransactionStatus(..)) && args(transactionId)")
    public TransactionStatus getTransactionStatus(
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
                null,
                true
        );
    }
}
