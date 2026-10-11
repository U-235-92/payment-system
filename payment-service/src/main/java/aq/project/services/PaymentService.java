package aq.project.services;

import aq.project.dto.*;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.exceptions.*;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.handlers.PaymentProviderServiceHandler;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.mappers.PaymentMapper;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${service.payment-provider-service.name}")
    private String paymentProviderServiceName;

    private final PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    private final PaymentServiceHandler paymentServiceHandler;
    private final PaymentProviderServiceHandler paymentProviderServiceHandler;

    private final Fallback fallback;

    @RateLimiter(
            name = "create-payment-rate-limiter",
            fallbackMethod = "createPaymentFallback"
    )
    @CircuitBreaker(
            name = "create-payment-circuitbreaker",
            fallbackMethod = "createPaymentFallback"
    )
    @Transactional(
            noRollbackFor = ProcessOperationException.class
    )
    public PaymentServiceCreatePaymentResponseDto createPayment(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto
    ) {
        UUID transactionId = paymentServiceCreatePaymentRequestDto.getTransactionId();

        Payment createdPayment = paymentServiceHandler.createPayment(paymentServiceCreatePaymentRequestDto);

        PaymentProviderServiceCreatePaymentResponseDto paymentResponseDto = paymentProviderServiceHandler
                .createPayment(paymentServiceCreatePaymentRequestDto);

        PaymentStatus receivedPaymentStatus = paymentResponseDto.getPaymentStatus();

        switch(receivedPaymentStatus) {
            case COMPLETED, PENDING -> {
                UUID receivedPaymentId = paymentResponseDto.getPaymentId();

                createdPayment.setPaymentId(receivedPaymentId);
                createdPayment.setStatus(receivedPaymentStatus);

                paymentRepository.save(createdPayment);

                return paymentMapper.toPaymentServiceCreatePaymentResponseDto(createdPayment);
            }
            default ->
                    throw new ProcessOperationException(String.format(
                            "Fail while creating payment for transaction with id: [%s] on [%s] side: " +
                            "received response with: [%s] payment status",
                            transactionId, paymentProviderServiceName, receivedPaymentStatus));
        }
    }

    private PaymentServiceCreatePaymentResponseDto createPaymentFallback(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto,
            Exception exception
    ) throws Exception {
        Predicate<Exception> fallbackExceptionPredicate = exc ->
                exc instanceof ProcessOperationException
                || exc instanceof ProhibitedOperationException
                || exc instanceof EntityAlreadyExistsException
                || exc instanceof EntityNotFoundException
                || exc instanceof ServiceHttpException
                || exc instanceof ClientHttpException;

        if(fallbackExceptionPredicate.test(exception))
            throw exception;

        UUID transactionId = paymentServiceCreatePaymentRequestDto.getTransactionId();

        String action = "create-payment";
        String message = String.format(
                "Exception occurred during create of payment for transaction with id: [%s]",
                transactionId);

        fallback.handleBulkheadFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "get-payment-methods-rate-limiter",
            fallbackMethod = "getPaymentMethodsFallback"
    )
    public List<PaymentServicePaymentMethodResponseDto> getPaymentMethods(
            String currencyCode,
            String countryCode
    ) {
        List<PaymentMethod> paymentMethodList = paymentMethodRepository.findAllByCurrencyCodeAndCountryCode(currencyCode, countryCode);

        List<PaymentServicePaymentMethodResponseDto> paymentMethodDtoList = new ArrayList<>();

        for(PaymentMethod paymentMethod : paymentMethodList) {
            PaymentServicePaymentMethodResponseDto dto = paymentMapper.toPaymentServicePaymentMethodResponseDto(paymentMethod);
            paymentMethodDtoList.add(dto);
        }
        return paymentMethodDtoList;
    }

    private List<PaymentServicePaymentMethodResponseDto> getPaymentMethodsFallback(
            String currencyCode,
            String countryCode,
            Exception exception
    ) {
        String action = "get-payment-methods";
        String message = String.format(
                "Exception occurred during getting of payment methods for currency code: [%s] and country code: [%s]",
                currencyCode, countryCode);

        fallback.handleBulkheadFallback(action, message, exception);

        return null;
    }
}