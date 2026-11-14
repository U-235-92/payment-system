package aq.project.aspects;

import aq.project.entities.Payout;
import aq.project.entities.Transaction;
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

@Aspect
@Order(1)
@Validated
@Component
@RequiredArgsConstructor
public class WebhookServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.WebhookService.completeTransactionAsync(..)) " +
            "&& args(transaction)")
    public void completeTransactionAsync(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Transaction transaction
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "complete-transaction-async";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format(
                "Received request to update transaction status with transaction id: [%s]", transaction.getId());

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

    @Around("execution(* aq.project.services.WebhookService.completePayoutAsync(..)) " +
            "&& args(payout)")
    public void completePayoutAsync(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Payout payout
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "complete-payout-async";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to update payout status with payout id: [%s]",
                payout.getId());

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
