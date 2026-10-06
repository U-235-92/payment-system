package aq.project.services;

import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentProviderServiceFailPaymentRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.PaymentRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.mappers.PaymentMapper;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;

    private final Fallback fallback;

    @Transactional
    @RateLimiter(
            name = "create-payment-rate-limiter",
            fallbackMethod = "createPaymentRateLimiterFallback"
    )
    public PaymentProviderServiceCreatePaymentResponseDto createPayment(
            PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto
    ) {
        UUID transactionId = paymentProviderServiceCreatePaymentRequestDto.getTransactionId();
        String traceId = paymentProviderServiceCreatePaymentRequestDto.getTraceId();

        checkPaymentNotExistForTransaction(transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id [%s] not found", transactionId)));

        Payment newPayment = paymentMapper.toPayment(paymentProviderServiceCreatePaymentRequestDto);
        newPayment.setId(UUID.randomUUID());
        newPayment.setTransaction(transaction);

        Payment savedPayment = paymentRepository.save(newPayment);

        PaymentProviderServiceCreatePaymentResponseDto dto = paymentMapper.toPaymentProviderServiceCreatePaymentResponseDto(savedPayment);
        dto.setTraceId(traceId);

        return dto;
    }

    private PaymentProviderServiceCreatePaymentResponseDto createPaymentRateLimiterFallback(
            PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto,
            Exception exception
    ) {
        UUID transactionId = paymentProviderServiceCreatePaymentRequestDto.getTransactionId();

        String action = "create-payment";
        String message = String.format(
                "Exception occurred during creating of payment for transaction with id: [%s]. Exception message: %s",
                transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    private void checkPaymentNotExistForTransaction(UUID transactionId) {
        if(paymentRepository.findByTransactionId(transactionId).isPresent())
            throw new ProhibitedOperationException(String.format(
                    "Payment for transaction with id: [%s] is already exist", transactionId));
    }

    @Transactional
    @RateLimiter(
            name = "fail-payment-rate-limiter",
            fallbackMethod = "failPaymentRateLimiterFallback"
    )
    public void failPayment(
            PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto
    ) {
        UUID paymentId = paymentProviderServiceFailPaymentRequestDto.getPaymentId();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Payment with id [%s] not found", paymentId)));

        PaymentStatus currentPaymentStatus = payment.getStatus();
        PaymentStatus targetPaymentStatus = PaymentStatus.FAILED;

        switch (currentPaymentStatus) {
            case PENDING, COMPLETED -> payment.setStatus(targetPaymentStatus);
            default -> throw new ProhibitedOperationException(
                    String.format(
                            "Attempt of changing payment status for payment with id: [%s] from: [%s] to [%s] " +
                            "which is prohibited operation", paymentId, currentPaymentStatus, targetPaymentStatus));
        }
    }

    private void failPaymentRateLimiterFallback(
            PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto,
            Exception exception
    ) {
        UUID paymentId = paymentProviderServiceFailPaymentRequestDto.getPaymentId();
        UUID transactionId = paymentProviderServiceFailPaymentRequestDto.getTransactionId();

        String action = "fail-payment";
        String message = String.format(
                "Exception occurred during fail of payment with id: [%s] for transaction with id: [%s]. " +
                "Exception message: %s", paymentId, transactionId, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);
    }
}
