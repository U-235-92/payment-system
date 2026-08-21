package aq.project.utils.aspects;

import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionResponseDto;
import aq.project.dto.TransactionStatus;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static aq.project.utils.constants.RequestPropertyKeys.TRANSACTION_ID;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TransactionService.createTransaction(..)) && args(requestDto, merchantId)")
    public TransactionResponseDto createTransaction(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransactionRequestDto requestDto,
            @NotBlank String merchantId
    ) throws Throwable {
//      Check request DTO property key
        if(!requestDto.getProperties().containsKey(TRANSACTION_ID)) {
            String msg = "Received transaction request with no specified transaction id property key";
            throw new ConstraintViolationException(msg, null);
        }

//        Prepare handler metadata
        String actionName = "create-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String transactionId = requestDto.getProperties().get(TRANSACTION_ID);
        String preMainLogicLogMessage = String.format("Received request to create transaction with id: [%s] for merchant with id: [%s]",
                transactionId, merchantId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to create transaction with id: [%s] for merchant with id: [%s]",
                transactionId, merchantId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle create transaction with id: [%s] for merchant with id: [%s]",
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
                checkConstraints(requestDto),
                null,
                null
        );
    }

    private Supplier<Void> checkConstraints(TransactionRequestDto requestDto) {
        checkPositiveAmount(requestDto);
        checkIsExistTransactionIdProperty(requestDto);
        return null;
    }

    private void checkPositiveAmount(TransactionRequestDto requestDto) {
        try {
            BigDecimal amount = new BigDecimal(requestDto.getAmount());
            if(amount.compareTo(new BigDecimal(0)) <= 0) {
                String msg = "The amount of the transaction must be greater than zero";
                throw new ConstraintViolationException(msg, null);
            }
        } catch (NumberFormatException e) {
            throw new ConstraintViolationException(e.getMessage(), null);
        }
    }

    private void checkIsExistTransactionIdProperty(TransactionRequestDto requestDto) {
        if(requestDto.getProperties().get(TRANSACTION_ID) == null || requestDto.getProperties().get(TRANSACTION_ID).isBlank()) {
            String msg = "Received transaction request with no specified transaction id property value";
            throw new ConstraintViolationException(msg, null);
        }
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

    @Around("execution(* aq.project.services.TransactionService.cancelTransaction(..)) && args(transactionId, merchantId, transactionStatus)")
    public void cancelTransaction(
            ProceedingJoinPoint pjp,
            @NotNull UUID transactionId,
            @NotBlank String merchantId,
            @NotNull TransactionStatus transactionStatus
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "cancel-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to cancel transaction with id: [%s] for merchant with id: [%s]",
                transactionId, merchantId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to cancel transaction with id: [%s] for merchant with id: [%s]",
                transactionId, merchantId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle cancel transaction with id: [%s] for merchant with id: [%s]",
                transactionId, merchantId);

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
}
