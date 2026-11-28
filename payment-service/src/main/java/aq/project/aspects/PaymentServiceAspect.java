package aq.project.aspects;

import aq.project.dto.CreatePaymentRequestDto;
import aq.project.dto.CreatePaymentResponseDto;
import aq.project.dto.PaymentMethodResponseDto;
import aq.project.entities.Payment;
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Aspect
@Order(1)
@Validated
@Component
@RequiredArgsConstructor
public class PaymentServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.PaymentService.createPayment(..)) " +
            "&& args(createPaymentRequestDto)")
    public CreatePaymentResponseDto createPayment(
            ProceedingJoinPoint pjp,
            @NotNull @Valid CreatePaymentRequestDto createPaymentRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-payment";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to create a new payment for transaction with id: [%s]",
                createPaymentRequestDto.getTransactionId());

//        Handler logic call
        return serviceAspectHandler.handle(
                CreatePaymentResponseDto.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.PaymentService.getPaymentMethods(..)) " +
            "&& args(currencyCode, countryCode)")
    public List<PaymentMethodResponseDto> getPaymentMethods(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "[A-Z]{3}") String currencyCode,
            @NotBlank @Pattern(regexp = "[A-Z]{3}") String countryCode
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-payment-methods";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format(
                "Received request to get payment methods for currency: [%s] and country: [%s]",
                currencyCode, countryCode);

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

    @Around("execution(* aq.project.services.PaymentService.handlePendingPayment(..)) " +
            "&& args(payment)")
    public void handlePendingPayment(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Payment payment
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "handle-pending-payment";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format(
                "Received request to handle of payment in [%s] status for transaction with id: [%s]",
                payment.getStatus(), payment.getTransactionId());

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