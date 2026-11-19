package aq.project.aspects;

import aq.project.dto.PaymentMethodResponseDto;
import aq.project.telemetry.ServiceAspectHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class PaymentServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.PaymentService.getPaymentMethods(..)) " +
            "&& args(currencyCode, countryCode)")
    public Mono<Flux<PaymentMethodResponseDto>> getPaymentMethods(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "[A-Z]{3}") String currencyCode,
            @NotBlank @Pattern(regexp = "[A-Z]{3}") String countryCode
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-payment-methods";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = String.format(
                "Received request for get payment methods for currency code: [%s] and country code: [%s]",
                currencyCode, countryCode);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle of request for get payment methods for currency code: [%s] and country code: [%s]",
                currencyCode, countryCode);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred while handle of request for get payment methods for currency code: [%s] and country code: [%s]",
                currencyCode, countryCode);

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