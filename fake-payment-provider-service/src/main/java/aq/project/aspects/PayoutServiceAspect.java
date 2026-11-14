package aq.project.aspects;

import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PayoutInfoResponseDto;
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
public class PayoutServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.PayoutService.createPayout(..)) " +
            "&& args(createPayoutRequestDto, authentication)")
    public PayoutInfoResponseDto createPayout(
            ProceedingJoinPoint pjp,
            @NotNull @Valid CreatePayoutRequestDto createPayoutRequestDto,
            @NotNull Authentication authentication
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-payout";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to create a new payout for merchant with id: [%s]",
                createPayoutRequestDto.getMerchantId());

//        Handler logic call
        return serviceAspectHandler.handle(
                PayoutInfoResponseDto.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.PayoutService.getPayoutList(..)) " +
            "&& args(startDate, endDate, authentication)")
    public List<PayoutInfoResponseDto> getPayoutList(
            ProceedingJoinPoint pjp,
            @Nullable OffsetDateTime startDate,
            @Nullable OffsetDateTime endDate,
            @NotNull Authentication authentication
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-payout-list";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to get list of payout info for merchant with id: [%s]",
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

    @Around("execution(* aq.project.services.PayoutService.getPayoutInfo(..)) " +
            "&& args(id, authentication)")
    public PayoutInfoResponseDto getPayoutInfo(
            ProceedingJoinPoint pjp,
            @NotNull Long id,
            @NotNull Authentication authentication
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-payout-info";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to get payout info for payout with id: [%s] " +
                "and merchant with id: [%s]", id, authentication.getName());

//        Handler logic call
        return serviceAspectHandler.handle(
                PayoutInfoResponseDto.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }
}
