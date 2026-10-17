package aq.project.utils.aspects;

import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServicePaymentMethodResponseDto;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
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

    private final PaymentServiceHandler paymentServiceHandler;

    @Around(
            value = "execution(* aq.project.services.PaymentService.createPayment(..)) " +
                    "&& args(paymentServiceCreatePaymentRequestDto)"
    )
    public PaymentServiceCreatePaymentResponseDto createPayment(
            ProceedingJoinPoint pjp,
            @NotNull @Valid PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        UUID transactionId = paymentServiceCreatePaymentRequestDto.getTransactionId();

        String actionName = "create-payment";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format(
                "Received request to create payment for transaction with id: [%s]", transactionId);
        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle of request to create payment for transaction with id: [%s]", transactionId);
        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle of request to create payment for transaction with id: [%s]", transactionId);

//        Handler logic call
        return serviceAspectHandler.handle(
                PaymentServiceCreatePaymentResponseDto.class,
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

    @AfterThrowing(
            value = "execution(* aq.project.services.PaymentService.createPayment(..)) " +
                    "&& args(paymentServiceCreatePaymentRequestDto)",
            throwing = "exception"
    )
    public void createPayment(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto,
            Exception exception
    ) {
        UUID transactionId = paymentServiceCreatePaymentRequestDto.getTransactionId();

        paymentServiceHandler.setPaymentOnFailStatus(transactionId);
    }

    @Around(
            value = "execution(* aq.project.services.PaymentService.getPaymentMethods(..)) " +
                    "&& args(currencyCode, countryCode)"
    )
    public List<PaymentServicePaymentMethodResponseDto> getPaymentMethods(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "[A-Z]{3}$") String currencyCode,
            @NotBlank @Pattern(regexp = "[A-Z]{3}$") String countryCode
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-payment-methods";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format(
                "Received request to get payment methods for currency code: [%s] and country code: [%s]",
                currencyCode, countryCode);
        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle of request to get payment methods for currency code: [%s] and country code: [%s]",
                currencyCode, countryCode);
        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle of request to get payment methods for currency code: [%s] and country code: [%s]",
                currencyCode, countryCode);

//        Handler logic call
        return serviceAspectHandler.handle(
                List.class,
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
