package aq.project.utils.aspects.transaction_service;

import aq.project.dto.IndividualsApiServiceDepositTransactionRequestDto;
import aq.project.dto.TransactionStatus;
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
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Supplier;

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class DepositTransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.transactions.DepositTransactionService.createDepositTransaction(..)) && args(transactionRequest)")
    public Mono<UUID> createDepositTransaction(
            ProceedingJoinPoint pjp,
            @NotNull @Valid IndividualsApiServiceDepositTransactionRequestDto transactionRequest
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-deposit-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to create deposit transaction";
        String postSuccessMainLogicCallLogMessage = "Request to create deposit transaction completed successfully";
        String postFailureMainLogicCallLogMessage = "Request to create deposit transaction completed with error";

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                validateIndividualsApiServiceDepositTransactionRequestDto(transactionRequest)
        );
    }

    private Supplier<Mono<Void>> validateIndividualsApiServiceDepositTransactionRequestDto(
            IndividualsApiServiceDepositTransactionRequestDto transactionRequest
    ) {
        return () -> {
            BigDecimal amount = transactionRequest.getAmount();

            if(amount.compareTo(new BigDecimal(0)) <= 0) {
                String logMessageOnError = String.format(
                        "Received deposit transaction request with invalid amount value: [%s]",
                        amount);
                return Mono.error(new ConstraintViolationException(logMessageOnError, null));
            }
            return Mono.empty();
        };
    }

    @Around("execution(* aq.project.services.transactions.DepositTransactionService.getDepositTransactionStatus(..)) && args(transactionId)")
    public Mono<TransactionStatus> getDepositTransactionStatus(
            ProceedingJoinPoint pjp,
            @NotNull UUID transactionId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-deposit-transaction-status";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format(
                "Received request to get transaction status for transaction with id: %s",
                transactionId);
        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle request to get transaction status for transaction with id: %s",
                transactionId);
        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle request to get transaction status for transaction with id: %s",
                transactionId);

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }
}
