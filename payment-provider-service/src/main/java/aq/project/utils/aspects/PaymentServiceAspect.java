package aq.project.utils.aspects;

import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentProviderServiceFailPaymentRequestDto;
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
@Component
@Validated
@RequiredArgsConstructor
public class PaymentServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.PaymentService.createPayment(..)) && args(paymentProviderServiceCreatePaymentRequestDto)")
    public PaymentProviderServiceCreatePaymentResponseDto createPayment(
            ProceedingJoinPoint pjp,
            @NotNull @Valid PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-payment";
        String tracerName = serviceName + "." + actionName + "-tracer";

        UUID transactionId = paymentProviderServiceCreatePaymentRequestDto.getTransactionId();

        String preMainLogicLogMessage = String.format(
                "Received request to create payment for transaction with id: [%s]", transactionId);
        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle request to create payment for transaction with id: [%s]", transactionId);
        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle request of creating payment for transaction with id: [%s]", transactionId);

//        Handler logic call
        return serviceAspectHandler.handle(
                PaymentProviderServiceCreatePaymentResponseDto.class,
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

    @Around("execution(* aq.project.services.PaymentService.failPayment(..)) && args(paymentProviderServiceFailPaymentRequestDto)")
    public void failPayment(
            ProceedingJoinPoint pjp,
            @NotNull @Valid PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "fail-payment";
        String tracerName = serviceName + "." + actionName + "-tracer";

        UUID paymentId = paymentProviderServiceFailPaymentRequestDto.getPaymentId();
        UUID transactionId = paymentProviderServiceFailPaymentRequestDto.getTransactionId();

        String preMainLogicLogMessage = String.format(
                "Received request to fail payment with id: [%s] for transaction with id: [%s]",
                paymentId, transactionId);
        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle request to fail payment with id: [%s] for transaction with id: [%s]",
                paymentId, transactionId);
        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle request of creating payment with id: [%s] for transaction with id: [%s]",
                paymentId, transactionId);

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
