package aq.project.test.handlers.paymen_service_handler;

import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.handlers.PaymentServiceHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.entities.PaymentServiceEntities.*;

@ExtendWith(MockitoExtension.class)
public class SetPaymentOnFailStatusUnitTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceHandler paymentServiceHandler;

    @Test
    public void successSetPaymentOnFailStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Payment payment = getValidPayment();
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.PENDING);

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(payment));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentServiceHandler.failPayment(transactionId));

        Mockito.verify(paymentRepository, Mockito.times(1))
                .save(Mockito.any(Payment.class));
    }

    @Test
    public void failSetPaymentOnFailStatusOnPaymentNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Payment payment = getValidPayment();
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.PENDING);

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> paymentServiceHandler.failPayment(transactionId));

        Mockito.verify(paymentRepository, Mockito.never())
                .save(Mockito.any(Payment.class));
    }

    @Test
    public void failSetPaymentOnFailStatusOnPaymentIsNotInValidPaymentStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Payment payment = getValidPayment();
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.FAILED);

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(payment));

//        Act & Assert
        Assertions.assertThrows(ProhibitedOperationException.class,
                () -> paymentServiceHandler.failPayment(transactionId));

        Mockito.verify(paymentRepository, Mockito.never())
                .save(Mockito.any(Payment.class));
    }
}
