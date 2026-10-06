package aq.project.services.payment_service.unit;

import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
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
public class CreatePaymentUnitTest {

    @Spy
    private PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    public void successCreatePayment() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(transactionId);

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);

        Payment payment = getValidPayment();
        payment.setTransaction(transaction);

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(paymentRepository.save(Mockito.any(Payment.class)))
                        .thenReturn(payment);
        Mockito.when(transactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto));

        Mockito.verify(paymentRepository, Mockito.times(1)).save(Mockito.any(Payment.class));

        Assertions.assertEquals(paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto).getTransactionId(), transactionId);
    }

    @Test
    public void failCreatePaymentOnPaymentForTransactionAlreadyExists() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(transactionId);

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);

        Payment payment = getValidPayment();
        payment.setTransaction(transaction);

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(payment));

//        Act & Assert
        Assertions.assertThrows(ProhibitedOperationException.class,
                () -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto));

        Mockito.verify(paymentRepository, Mockito.never()).save(Mockito.any(Payment.class));
    }

    @Test
    public void failCreatePaymentOnTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(transactionId);

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);

        Payment payment = getValidPayment();
        payment.setTransaction(transaction);

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(transactionRepository.findById(Mockito.any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto));

        Mockito.verify(paymentRepository, Mockito.never()).save(Mockito.any(Payment.class));
    }
}
