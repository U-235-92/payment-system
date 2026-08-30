package aq.project.utils.aspects;

import aq.project.dto.CancelTransactionRequestDto;
import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.FailTransactionRequestDto;
import aq.project.dto.TransactionResponseDto;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TransactionService.handleCreateTransaction(..)) && args(requestDto)")
    public void handleCreateTransaction(
            ProceedingJoinPoint pjp,
            @Validated CreateTransactionRequestDto requestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "handle-create-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String transactionId = requestDto.getTransactionId().toString();
        String merchantId = requestDto.getMerchantId();
        String preMainLogicLogMessage = String.format("Start process operation: [%s] for transaction with id: [%s] and merchant id: [%s]",
                actionName, transactionId, merchantId);
        String postSuccessMainLogicCallLogMessage = String.format("Finish process operation: [%s] for transaction with id: [%s] and merchant id: [%s]",
                actionName, transactionId, merchantId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during process operation: [%s] for transaction with id: [%s] and merchant id: [%s]",
                actionName, transactionId, merchantId);

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
                null
        );
    }

    @Around("execution(* aq.project.services.TransactionService.handleFailTransaction(..)) && args(requestDto)")
    public void handleFailTransaction(
            ProceedingJoinPoint pjp,
            @Validated FailTransactionRequestDto requestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "handle-fail-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String transactionId = requestDto.getTransactionId().toString();
        String merchantId = requestDto.getMerchantId();
        String preMainLogicLogMessage = String.format("Start process operation: [%s] for transaction with id: [%s] and merchant id: [%s]",
                actionName, transactionId, merchantId);
        String postSuccessMainLogicCallLogMessage = String.format("Finish process operation: [%s] for transaction with id: [%s] and merchant id: [%s]",
                actionName, transactionId, merchantId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during process operation: [%s] for transaction with id: [%s] and merchant id: [%s]",
                actionName, transactionId, merchantId);

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
                null
        );
    }

    @Around("execution(* aq.project.services.TransactionService.handleCancelTransaction(..)) && args(requestDto)")
    public void handleCancelTransaction(
            ProceedingJoinPoint pjp,
            @Validated CancelTransactionRequestDto requestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "handle-cancel-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String transactionId = requestDto.getTransactionId().toString();
        String merchantId = requestDto.getMerchantId();
        String preMainLogicLogMessage = String.format("Start process operation: [%s] for transaction with id: [%s] and merchant id: [%s]",
                actionName, transactionId, merchantId);
        String postSuccessMainLogicCallLogMessage = String.format("Finish process operation: [%s] for transaction with id: [%s] and merchant id: [%s]",
                actionName, transactionId, merchantId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during process operation: [%s] for transaction with id: [%s] and merchant id: [%s]",
                actionName, transactionId, merchantId);

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
                null
        );
    }

    @Around("execution(* aq.project.services.TransactionService.getTransactionInfo(..)) && args(transactionId, merchantId)")
    public TransactionResponseDto getTransactionInfo(
            ProceedingJoinPoint pjp,
            @NotNull UUID transactionId,
            @NotBlank String merchantId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transaction-info";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get transaction info with id: [%s] for merchant with id: [%s]",
                transactionId, merchantId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to get transaction info with id: [%s] for merchant with id: [%s]",
                transactionId, merchantId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle get transaction info with id: [%s] for merchant with id: [%s]",
                transactionId, merchantId);

//        Handler logic call
        return serviceAspectHandler.handle(
                TransactionResponseDto.class,
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

    @Around("execution(* aq.project.services.TransactionService.getTransactionList(..)) && args(startDate, endDate, merchantId)")
    public List<TransactionResponseDto> getTransactionList(
            ProceedingJoinPoint pjp,
            @NotNull OffsetDateTime startDate,
            @NotNull OffsetDateTime endDate,
            @NotBlank String merchantId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transaction-list";
        String tracerName = serviceName + "." + actionName + "-tracer";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String preMainLogicLogMessage = String.format("Received request to get transaction list with for period: [%s -> %s] for merchant with id: [%s]",
                startDate.format(formatter), endDate.format(formatter), merchantId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to get transaction list with for period: [%s -> %s] for merchant with id: [%s]",
                startDate.format(formatter), endDate.format(formatter), merchantId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle get transaction list with for period: [%s -> %s] for merchant with id: [%s]",
                startDate.format(formatter), endDate.format(formatter), merchantId);

//        Handler logic call
        return serviceAspectHandler.handle(
                List.class,
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
