package aq.project.utils.aspects;

import aq.project.dto.MerchantRegistrationRequestDto;
import aq.project.dto.MerchantRegistrationResponseDto;
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
@Component
@Validated
@RequiredArgsConstructor
public class MerchantAdminServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.MerchantAdminService.registerMerchant(..)) && args(requestDto)")
    public MerchantRegistrationResponseDto registerMerchant(
            ProceedingJoinPoint pjp,
            @NotNull @Valid MerchantRegistrationRequestDto requestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "register-merchant";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to create merchant with id: [%s]",
                requestDto.getMerchantId());
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to create merchant with id: [%s]",
                requestDto.getMerchantId());
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle create merchant with id: [%s]",
                requestDto.getMerchantId());
//        Handler logic call
        return serviceAspectHandler.handle(
                MerchantRegistrationResponseDto.class,
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
