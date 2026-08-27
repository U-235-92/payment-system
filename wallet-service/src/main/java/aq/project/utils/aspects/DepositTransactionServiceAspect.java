package aq.project.utils.aspects;

import aq.project.dto.TransactionStatus;
import aq.project.messages.requests.DepositTransactionRequest;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
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

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class DepositTransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.DepositTransactionService.handleTransactionRequest(..)) && args(request)")
    public void handleTransactionRequest(
            ProceedingJoinPoint pjp,
            @Valid DepositTransactionRequest request
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "handle-deposit-transaction-request";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String transactionId = request.getTransactionId();
        String preMainLogicLogMessage = String.format("Received request to handle deposit transaction with id: [%s]",
                transactionId);
        String postSuccessMainLogicCallLogMessage = String.format("Request to handle deposit transaction with id: [%s] completed successfully",
                transactionId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle deposit transaction with id: [%s]",
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
            DepositTransactionRequest request
    ) {
        String transactionId = request.getTransactionId();
        String walletId = request.getWalletId();
        BigDecimal amount = request.getAmount();
        BigDecimal conversionRate = request.getConversionRate();

        checkUuid(transactionId);
        checkUuid(walletId);
        checkAmount(amount);
        checkConversionRate(conversionRate);

        return null;
    }

    private void checkUuid(String uuid) {
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        if(!matcher.find()) {
            String msg = String.format("Received deposit transaction request with invalid UUID value: [%s]", uuid);
            throw new ConstraintViolationException(msg, null);
        }
    }

    private void checkAmount(BigDecimal amount) {
        if(amount.compareTo(new BigDecimal(0)) <= 0) {
            String msg = "Received deposit transaction request with invalid amount value: " + amount;
            throw new ConstraintViolationException(msg, null);
        }
    }

    private void checkConversionRate(BigDecimal rate) {
        if(rate.compareTo(new BigDecimal(0)) <= 0) {
            String msg = "Received deposit transaction request with invalid conversion rate value: " + rate;
            throw new ConstraintViolationException(msg, null);
        }
    }

    @Around("execution(* aq.project.services.DepositTransactionService.getTransactionStatus(..)) && args(transactionId)")
    public TransactionStatus getTransactionStatus(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String transactionId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-deposit-transaction-status";
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
