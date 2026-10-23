package aq.project.test.handlers.paymen_service_handler;

import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.handlers.PaymentServiceHandler;
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
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentServiceHandler paymentServiceHandler;

    @Test
    public void successCreatePayment() {
//        Arrange
        Long paymentMethodId = 1L;

        UUID transactionId = UUID.randomUUID();

        PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        paymentServiceCreatePaymentRequestDto.setTransactionId(transactionId);
        PaymentMethod paymentMethod = getValidPaymentMethod();
        paymentMethod.setId(paymentMethodId);

        Payment payment = getValidPayment();
        payment.setPaymentId(null);
        payment.setTransactionId(transactionId);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PENDING);

        PaymentProviderServiceCreatePaymentResponseDto paymentProviderServiceCreatePaymentResponseDto = getValidPaymentProviderServiceCreatePaymentResponseDto();
        paymentProviderServiceCreatePaymentResponseDto.setTransactionId(transactionId);
        paymentProviderServiceCreatePaymentResponseDto.setPaymentStatus(PaymentStatus.COMPLETED);

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(paymentMethodRepository.findById(Mockito.any(Long.class)))
                .thenReturn(Optional.of(paymentMethod));
        Mockito.when(paymentRepository.save(Mockito.any(Payment.class)))
                .thenReturn(payment);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentServiceHandler.createPayment(paymentServiceCreatePaymentRequestDto));

        Mockito.verify(paymentRepository, Mockito.times(1))
                .save(Mockito.any(Payment.class));
    }

    @Test
    public void failCreatePaymentOnPaymentForTransactionAlreadyExists() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        paymentServiceCreatePaymentRequestDto.setTransactionId(transactionId);

        Payment payment = getValidPayment();
        payment.setTransactionId(transactionId);

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(payment));

//        Act & Assert
        Assertions.assertThrows(EntityAlreadyExistsException.class,
                () -> paymentServiceHandler.createPayment(paymentServiceCreatePaymentRequestDto));

        Mockito.verify(paymentRepository, Mockito.never())
                .save(Mockito.any(Payment.class));
    }

    @Test
    public void failCreatePaymentOnPaymentMethodForPaymentNotExists() {
//        Arrange
        PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(paymentMethodRepository.findById(Mockito.any(Long.class)))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> paymentServiceHandler.createPayment(paymentServiceCreatePaymentRequestDto));

        Mockito.verify(paymentRepository, Mockito.never())
                .save(Mockito.any(Payment.class));
    }
}
