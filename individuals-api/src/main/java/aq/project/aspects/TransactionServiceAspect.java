package aq.project.aspects;

import aq.project.dto.*;
import aq.project.telemetry.ServiceAspectHandler;
import jakarta.annotation.Nullable;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TransactionService.initTransaction(..)) " +
            "&& args(type, initTransactionRequestDto)")
    public Mono<InitTransactionResponseDto> initTransaction(
            ProceedingJoinPoint pjp,
            @NotNull PaymentType type,
            @NotNull @Valid InitTransactionRequestDto initTransactionRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "init-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = String.format(
                "Received request for [%s] transaction initialization with wallet id: [%s]",
                initTransactionRequestDto.getPaymentType(), initTransactionRequestDto.getWalletId());

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle of request for [%s] transaction initialization with wallet id: [%s]",
                initTransactionRequestDto.getPaymentType(), initTransactionRequestDto.getWalletId());

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred while handle of request for [%s] transaction initialization with wallet id: [%s]",
                initTransactionRequestDto.getPaymentType(), initTransactionRequestDto.getWalletId());

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

    @Around("execution(* aq.project.services.TransactionService.confirmTransaction(..)) " +
            "&& args(type, confirmTransactionRequestDto)")
    public Mono<UUID> confirmTransaction(
            ProceedingJoinPoint pjp,
            @NotNull PaymentType type,
            @NotNull @Valid ConfirmTransactionRequestDto confirmTransactionRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "confirm-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = String.format(
                "Received request for [%s] transaction confirm with wallet id: [%s]",
                confirmTransactionRequestDto.getPaymentType(), confirmTransactionRequestDto.getWalletId());

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle of request for [%s] transaction confirm with wallet id: [%s]",
                confirmTransactionRequestDto.getPaymentType(), confirmTransactionRequestDto.getWalletId());

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred while handle of request for [%s] transaction confirm with wallet id: [%s]",
                confirmTransactionRequestDto.getPaymentType(), confirmTransactionRequestDto.getWalletId());

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

    @Around("execution(* aq.project.services.TransactionService.getTransactionStatus(..)) " +
            "&& args(id)")
    public Mono<TransactionStatus> getTransactionStatus(
            ProceedingJoinPoint pjp,
            @NotNull UUID id
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transaction-status";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = String.format(
                "Received request for get transaction status with transaction id: [%s]", id);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle of request for get transaction status with transaction id: [%s]", id);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred while handle of request for get transaction status with transaction id: [%s]", id);

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

    @Around("execution(* aq.project.services.TransactionService.getTransactions(..)) " +
            "&& args(userId, walletId, type, status, dateFrom, dateTo, page, size)")
    public Mono<Flux<TransactionInfoResponseDto>> getTransactions(
            ProceedingJoinPoint pjp,
            @Nullable UUID userId,
            @Nullable UUID walletId,
            @Nullable PaymentType type,
            @Nullable TransactionStatus status,
            @Nullable OffsetDateTime dateFrom,
            @Nullable OffsetDateTime dateTo,
            @Nullable Integer page,
            @Nullable Integer size
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transactions";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = "Received request for get list of transactions";

        String postSuccessMainLogicCallLogMessage = "Success handle of request for getting list of transactions";

        String postFailureMainLogicCallLogMessage = "Error occurred while handle of request for getting list of transactions";

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