package aq.project.aspects;

import aq.project.dto.*;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Aspect
@Order(1)
@Validated
@Component
@RequiredArgsConstructor
public class TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TransactionService.initTransaction(..)) " +
            "&& args(initTransactionRequestDto)")
    public InitTransactionResponseDto initTransaction(
            ProceedingJoinPoint pjp,
            @NotNull @Valid InitTransactionRequestDto initTransactionRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "init-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to init [%s] transaction for wallet with id: [%s]",
                initTransactionRequestDto.getPaymentType(), initTransactionRequestDto.getWalletId());

//        Handler logic call
        return serviceAspectHandler.handle(
                InitTransactionResponseDto.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.TransactionService.confirmTransaction(..)) " +
            "&& args(confirmTransactionRequestDto)")
    public UUID confirmTransaction(
            ProceedingJoinPoint pjp,
            @NotNull @Valid ConfirmTransactionRequestDto confirmTransactionRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "confirm-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to confirm [%s] transaction for wallet with id: [%s]",
                confirmTransactionRequestDto.getPaymentType(), confirmTransactionRequestDto.getWalletId());

//        Handler logic call
        return serviceAspectHandler.handle(
                UUID.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(void aq.project.services.TransactionService.confirmTransaction(..)) " +
            "&& args(transactionId)")
    public void confirmTransaction(
            ProceedingJoinPoint pjp,
            @NotNull UUID transactionId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "confirm-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to confirm transaction with id: [%s]", transactionId);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.TransactionService.getTransactionStatus(..)) " +
            "&& args(transactionId)")
    public TransactionStatus getTransactionStatus(
            ProceedingJoinPoint pjp,
            @NotNull UUID transactionId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transaction-status";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to get transaction status with id: [%s]",
                transactionId);

//        Handler logic call
        return serviceAspectHandler.handle(
                TransactionStatus.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.TransactionService.failTransaction(..)) " +
            "&& args(transactionId)")
    public void failTransaction(
            ProceedingJoinPoint pjp,
            @NotNull UUID transactionId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "fail-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to fail transaction with id: [%s]",
                transactionId);

//        Handler logic call
        serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.TransactionService.getTransactions(..)) " +
            "&& args(userId, walletId, type, status, dateFrom, dateTo, page, size)")
    public List<TransactionInfoResponseDto> getTransactions(
            ProceedingJoinPoint pjp,
            @Nullable UUID userId,
            @Nullable UUID walletId,
            @Nullable PaymentType type,
            @Nullable TransactionStatus status,
            @Nullable OffsetDateTime dateFrom,
            @Nullable OffsetDateTime dateTo,
            @Nullable @PositiveOrZero Integer page,
            @Nullable @Positive Integer size
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transactions";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = "Received request to get list of transaction info";

//        Handler logic call
        return serviceAspectHandler.handle(
                List.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }
}