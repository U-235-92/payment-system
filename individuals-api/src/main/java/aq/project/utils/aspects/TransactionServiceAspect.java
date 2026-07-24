package aq.project.utils.aspects;

import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.utils.constants.RequestPropertyKeys;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TransactionService.getTransactionStatus(..)) && args(transactionId)")
    public Mono<TransactionStatus> getTransactionStatus(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String transactionId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-transaction-status";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get transaction status for transaction with id: %s",
                transactionId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to get transaction status for transaction with id: %s",
                transactionId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to get transaction status for transaction with id: %s",
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

    @Around("execution(* aq.project.services.TransactionService.doTransaction(..)) && args(dto)")
    public Mono<String> doTransaction(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransactionRequestDto dto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "do-transaction";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String transactionOperation = dto.getOperationType().toString().toLowerCase();
        String suffixLogMessage = getSuffixLogMessage(dto);
        String preMainLogicLogMessage = String.format("Received request to process [%s] transaction. %s",
                transactionOperation, suffixLogMessage);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to process [%s] transaction. %s",
                transactionOperation, suffixLogMessage);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to process [%s] transaction. %s",
                transactionOperation, suffixLogMessage);
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

    private String getSuffixLogMessage(TransactionRequestDto dto) {
        return switch (dto.getOperationType()) {
            case DEPOSIT, WITHDRAW -> String.format("Recipient wallet id: %s",
                    getDtoProperty(dto, RequestPropertyKeys.RECIPIENT_WALLET_ID));
            case TRANSFER -> String.format("Sender wallet id: %s, Recipient wallet id: %s",
                    getDtoProperty(dto, RequestPropertyKeys.SENDER_WALLET_ID),
                    getDtoProperty(dto, RequestPropertyKeys.RECIPIENT_WALLET_ID));
        };
    }

    private String getDtoProperty(TransactionRequestDto dto, String property) {
        if(dto.getProperties().containsKey(property))
            return dto.getProperties().get(property);
        throw new IllegalArgumentException(property + " is null or empty");
    }
}
