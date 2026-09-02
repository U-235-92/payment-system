package aq.project.utils.aspects.transaction;

import aq.project.dto.TransactionStatus;
import aq.project.dto.TransferTransactionRequestDto;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolationException;
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

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Supplier;

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class TransferTransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.transaction.TransferTransactionService.handleTransactionRequest(..)) && args(request)")
    public void handleTransactionRequest(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransferTransactionRequestDto request
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "handle-transfer-transaction-request";
        String tracerName = serviceName + "." + actionName + "-tracer";
        UUID transactionId = request.getTransactionId();
        String preMainLogicLogMessage = String.format("Received request to handle withdraw transaction with id: [%s]",
                transactionId);
        String postSuccessMainLogicCallLogMessage = String.format("Request to handle withdraw transaction with id: [%s] completed successfully",
                transactionId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle withdraw transaction with id: [%s]",
                transactionId);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                checkConstraints(request),
                null,
                null
        );
    }

    private Supplier<Void> checkConstraints(
            TransferTransactionRequestDto request
    ) {
        BigDecimal senderConversionRate = request.getSenderConversionRate();
        BigDecimal recipientConversionRate = request.getRecipientConversionRate();
        BigDecimal amount = request.getAmount();

        checkConversionRate(senderConversionRate);
        checkConversionRate(recipientConversionRate);
        checkAmount(amount);

        return null;
    }

    private void checkAmount(BigDecimal amount) {
        if(amount.compareTo(new BigDecimal(0)) <= 0) {
            String msg = "Received withdraw transaction request with invalid amount value: " + amount;
            throw new ConstraintViolationException(msg, null);
        }
    }

    private void checkConversionRate(BigDecimal rate) {
        if(rate.compareTo(new BigDecimal(0)) <= 0) {
            String msg = "Received withdraw transaction request with invalid conversion rate value: " + rate;
            throw new ConstraintViolationException(msg, null);
        }
    }

    @Around("execution(* aq.project.services.transaction.TransferTransactionService.getTransactionStatus(..)) && args(transactionId)")
    public TransactionStatus getTransactionStatus(
            ProceedingJoinPoint pjp,
            @NotNull UUID transactionId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transfer-transaction-status";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get transaction status with id: [%s]",
                transactionId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle of request to get transaction status with id: [%s]",
                transactionId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during getting transaction status with id: [%s]",
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
