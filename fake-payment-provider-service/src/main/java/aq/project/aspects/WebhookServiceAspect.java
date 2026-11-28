package aq.project.aspects;

import aq.project.dto.OperationType;
import aq.project.dto.UpdateStatusOperationRequestDto;
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
@Validated
@Component
@RequiredArgsConstructor
public class WebhookServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.WebhookService.updateOperationStatus(..)) " +
            "&& args(updateStatusOperationRequestDto)")
    public void completePayoutAsync(
            ProceedingJoinPoint pjp,
            @NotNull @Valid UpdateStatusOperationRequestDto updateStatusOperationRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "update-operation-status";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String operationType = updateStatusOperationRequestDto.getOperationType();
        UUID operationId = updateStatusOperationRequestDto.getId();

        String logOnBeginning = String.format(
                "Received request to update [%s] status with [%s] id: [%s]",
                operationType, operationType, operationId);

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
}