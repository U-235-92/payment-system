package aq.project.services.payment_service.unit;

import aq.project.dto.PaymentProviderServiceFailPaymentRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.PaymentRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.services.PaymentService;
import aq.project.utils.mappers.PaymentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.entities.PaymentServiceEntities.*;

@ExtendWith(MockitoExtension.class)
public class FailPaymentUnitTest {

    @Spy
    private PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    public void successFailPayment() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto = getValidPaymentProviderServiceFailPaymentRequestDto();
        paymentProviderServiceFailPaymentRequestDto.setTransactionId(transactionId);

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);

        Payment payment = getValidPayment();
        payment.setTransaction(transaction);

        Mockito.when(paymentRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(payment));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.failPayment(paymentProviderServiceFailPaymentRequestDto));
        Assertions.assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    public void failFailPaymentOnPaymentNotFound() {
//        Arrange
        PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto = getValidPaymentProviderServiceFailPaymentRequestDto();

        Mockito.when(paymentRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> paymentService.failPayment(paymentProviderServiceFailPaymentRequestDto));
    }

    @Test
    public void failFailPaymentOnPaymentInNotValidState() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto = getValidPaymentProviderServiceFailPaymentRequestDto();
        paymentProviderServiceFailPaymentRequestDto.setTransactionId(transactionId);

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);

        Payment payment = getValidPayment();
        payment.setTransaction(transaction);
        payment.setStatus(PaymentStatus.FAILED);

        Mockito.when(paymentRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(payment));

//        Act & Assert
        Assertions.assertThrows(ProhibitedOperationException.class,
                () -> paymentService.failPayment(paymentProviderServiceFailPaymentRequestDto));
    }
}
