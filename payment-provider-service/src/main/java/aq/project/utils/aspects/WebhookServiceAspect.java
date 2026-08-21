package aq.project.utils.aspects;

import aq.project.dto.TransactionStatusDto;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Aspect
@Validated
@Component
@RequiredArgsConstructor
public class WebhookServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.WebhookService.updateTransactionStatus(..)) && args(transactionStatusDto)")
    public void updateTransactionStatus(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransactionStatusDto transactionStatusDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "update-transaction-status";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to update transaction status with id: [%s], event type: [%s]",
                transactionStatusDto.getId(), transactionStatusDto.getEventType().getValue());
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to update transaction status with id: [%s], event type: [%s]",
                transactionStatusDto.getId(), transactionStatusDto.getEventType().getValue());
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle update transaction status with id: [%s], event type: [%s]",
                transactionStatusDto.getId(), transactionStatusDto.getEventType().getValue());
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
