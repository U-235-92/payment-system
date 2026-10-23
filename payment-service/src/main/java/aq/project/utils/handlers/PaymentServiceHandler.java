package aq.project.utils.handlers;

import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.mappers.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentServiceHandler {

    private final PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment createPayment(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto
    ) {
        UUID transactionId = paymentServiceCreatePaymentRequestDto.getTransactionId();
        Long paymentMethodId = paymentServiceCreatePaymentRequestDto.getPaymentMethodId();

        Optional<Payment> optionalPayment = paymentRepository.findByTransactionId(transactionId);

        if(optionalPayment.isPresent())
            throw new EntityAlreadyExistsException(String.format(
                    "Payment for transaction with id: [%s] already exists", transactionId));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "Payment method with id: [%s] for transaction with id: [%s] does not exist",
                        paymentMethodId, transactionId)));

        Payment newPayment = paymentMapper.toPayment(paymentServiceCreatePaymentRequestDto);
        newPayment.setPaymentMethod(paymentMethod);

        return paymentRepository.save(newPayment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failPayment(
            UUID transactionId
    ) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Payment for transaction with id: [%s] not found", transactionId)));

        PaymentStatus currentPaymentStatus = payment.getStatus();
        PaymentStatus targetPaymentStatus = PaymentStatus.FAILED;

        switch (currentPaymentStatus) {
            case COMPLETED, PENDING -> payment.setStatus(targetPaymentStatus);

            default -> throw new ProhibitedOperationException(String.format(
                    "Error occurred while setting payment status for transaction with id: [%s]. " +
                    "Attempt to change payment status from: [%s] to: [%s] which is prohibited.",
                    transactionId, currentPaymentStatus, targetPaymentStatus));
        }
        paymentRepository.save(payment);
    }
}
