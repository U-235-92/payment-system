package aq.project.aspects;

import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.TransactionInfoResponseDto;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.List;

@Aspect
@Order(1)
@Validated
@Component
@RequiredArgsConstructor
public class TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TransactionService.createTransaction(..)) " +
            "&& args(createTransactionRequestDto, authentication)")
    public TransactionInfoResponseDto createTransaction(
            ProceedingJoinPoint pjp,
            @NotNull @Valid CreateTransactionRequestDto createTransactionRequestDto,
            @NotNull Authentication authentication
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to create a new transaction for merchant with id: [%s]",
                createTransactionRequestDto.getMerchantId());

//        Handler logic call
        return serviceAspectHandler.handle(
                TransactionInfoResponseDto.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.TransactionService.getTransactionList(..)) " +
            "&& args(startDate, endDate, authentication)")
    public List<TransactionInfoResponseDto> getTransactionList(
            ProceedingJoinPoint pjp,
            @Nullable OffsetDateTime startDate,
            @Nullable OffsetDateTime endDate,
            @NotNull Authentication authentication
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transaction-list";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to get list of transaction info for merchant with id: [%s]",
                authentication.getName());

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

    @Around("execution(* aq.project.services.TransactionService.getTransactionInfo(..)) " +
            "&& args(id, authentication)")
    public TransactionInfoResponseDto getTransactionInfo(
            ProceedingJoinPoint pjp,
            @NotNull Long id,
            @NotNull Authentication authentication
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transaction-info";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to get transaction info for transaction with id: [%s] " +
                        "and merchant with id: [%s]", id, authentication.getName());

//        Handler logic call
        return serviceAspectHandler.handle(
                TransactionInfoResponseDto.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }
}
