package aq.project.services;

import aq.project.entities.Payment;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    private final PaymentRepository paymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessed(UUID paymentId) {
        Payment payment = getPayment(paymentId);

        payment.markProcessed();
    }

    private Payment getPayment(
            UUID paymentId
    ) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Payment not found for payment id: [%s]", paymentId)));
    }
}