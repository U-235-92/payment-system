package aq.project.utils.aspect;

import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
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

import java.util.UUID;

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class TransferTransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TransferTransactionService.createTransaction(..)) && args(transactionServiceTransferTransaction)")
    public UUID createTransaction(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransactionServiceTransferTransaction transactionServiceTransferTransaction
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-transfer-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to create transfer transaction";
        String postSuccessMainLogicCallLogMessage = "Success handle of request to create transfer transaction";
        String postFailureMainLogicCallLogMessage = "Error occurred during handle of request to create transfer transaction";

//        Handler logic call
        return serviceAspectHandler.handle(
                UUID.class,
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
                false
        );
    }

    @Around("execution(* aq.project.services.TransferTransactionService.getTransactionStatus(..)) && args(transactionId)")
    public TransactionStatus getTransactionStatus(
            ProceedingJoinPoint pjp,
            @NotNull UUID transactionId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transfer-transaction-status";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to get transfer transaction status";
        String postSuccessMainLogicCallLogMessage = "Success handle of request to get transfer transaction status";
        String postFailureMainLogicCallLogMessage = "Error occurred during handle of request to get transfer transaction status";

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
                false
        );
    }
}
