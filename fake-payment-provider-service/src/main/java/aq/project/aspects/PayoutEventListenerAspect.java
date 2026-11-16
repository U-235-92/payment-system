package aq.project.aspects;

import aq.project.entities.Payout;
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
public class PayoutEventListenerAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.listeners.PayoutEventListener.onCommitPayout(..)) " +
            "&& args(payout)")
    public void onCommitPayout(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Payout payout
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "on-commit-payout";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format(
                "Received request to update payout status with payout id: [%s]", payout.getId());

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