package aq.project.utils.handlers;

import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentHandler {

    @Value("${service.payment-provider-service.name}")
    private String paymentProviderServiceName;

    private final PaymentRepository paymentRepository;

    private final PaymentServiceHandler paymentServiceHandler;
    private final PaymentProviderServiceHandler paymentProviderServiceHandler;

    @Transactional(
            noRollbackFor = ProcessOperationException.class
    )
    public Payment createPayment(
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

                return paymentRepository.save(createdPayment);
            }
            default -> {
                throw new ProcessOperationException(String.format(
                        "Fail while creating payment for transaction with id: [%s] on [%s] side: " +
                        "received response with: [%s] payment status",
                        transactionId, paymentProviderServiceName, receivedPaymentStatus));
            }
        }
    }
}
