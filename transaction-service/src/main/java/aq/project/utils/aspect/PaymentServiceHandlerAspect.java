package aq.project.utils.aspect;

import aq.project.dto.CancelTransactionDto;
import aq.project.messages.TransactionRequest;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

@Slf4j
@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class PaymentServiceHandlerAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.utils.handlers.PaymentServiceHandler.sendTransactionRequestToPaymentService(..)) && args(transactionRequest)")
    public void sendTransactionRequestToPaymentService(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransactionRequest transactionRequest
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "send-transaction-request-to-payment-service";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String operation = transactionRequest.getOperationType().toString().toLowerCase();
        String transactionId = transactionRequest.getTransactionId();
        String preMainLogicLogMessage = String.format("Received request to send [%s] transaction with id: [%s] to payment service",
                operation, transactionId);
        String postSuccessMainLogicCallLogMessage = String.format("Success request to send [%s] transaction with id: [%s] to payment service",
                operation, transactionId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during send [%s] transaction with id: [%s] to payment service",
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
                checkConstraints(transactionRequest),
                null,
                null,
                false
        );
    }

    private Supplier<Void> checkConstraints(TransactionRequest transactionRequest) {
        return () -> {
            BigDecimal amount = transactionRequest.getAmount();
            checkIsPositiveAmount(amount);
            return null;
        };
    }

    private void checkIsPositiveAmount(BigDecimal amount) {
        if(amount.compareTo(new BigDecimal(0)) <= 0) {
            String msg = "The amount of the transaction must be greater than zero";
            throw new ConstraintViolationException(msg, null);
        }
    }

    @Around("execution(* aq.project.utils.handlers.PaymentServiceHandler.sendCancelTransactionRequestToPaymentService(..)) && args(cancelTransactionDto)")
    public void sendCancelTransactionRequestToPaymentService(
            ProceedingJoinPoint pjp,
            @NotNull @Valid CancelTransactionDto cancelTransactionDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "send-cancel-transaction-request-to-payment-service";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String status = cancelTransactionDto.getStatus().toString().toLowerCase();
        String transactionId = cancelTransactionDto.getId();
        String preMainLogicLogMessage = String.format("Received request to cancel [%s] transaction with id: [%s] to payment service",
                status, transactionId);
        String postSuccessMainLogicCallLogMessage = String.format("Success request to cancel [%s] transaction with id: [%s] to payment service",
                status, transactionId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during cancel [%s] transaction with id: [%s] to payment service",
                status, transactionId);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                checkConstraints(cancelTransactionDto),
                null,
                null,
                false
        );
    }

    private Supplier<Void> checkConstraints(CancelTransactionDto cancelTransactionDto) {
        return () -> {
            String transactionId = cancelTransactionDto.getId();
            checkUuid(transactionId);
            return null;
        };
    }

    private void checkUuid(String uuid) {
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        if(!matcher.find())
            throw new ConstraintViolationException("No valid UUID", null);
    }
}
