package aq.project.test.handlers.payment_handler;

import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ServiceHttpException;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.handlers.PaymentHandler;
import aq.project.utils.handlers.PaymentProviderServiceHandler;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.mappers.PaymentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static aq.project._utils.entities.PaymentServiceEntities.*;

@ExtendWith(MockitoExtension.class)
public class CreatePaymentUnitTest {

    @Spy
    private PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentServiceHandler paymentServiceHandler;
    @Mock
    private PaymentProviderServiceHandler paymentProviderServiceHandler;

    @InjectMocks
    private PaymentHandler paymentHandler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(paymentHandler, "paymentProviderServiceName", "test-service");
    }

    @Test
    public void successCreatePayment() {
//        Arrange
        long paymentMethodId = 1L;

        PaymentServiceCreatePaymentRequestDto createPaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        createPaymentRequestDto.setPaymentMethodId(paymentMethodId);

        PaymentMethod paymentMethod = getValidPaymentMethod();
        paymentMethod.setId(paymentMethodId);

        Payment payment = paymentMapper.toPayment(createPaymentRequestDto);
        payment.setPaymentMethod(paymentMethod);

        PaymentProviderServiceCreatePaymentResponseDto createPaymentResponseDto = getValidPaymentProviderServiceCreatePaymentResponseDto();
        createPaymentResponseDto.setPaymentStatus(PaymentStatus.COMPLETED);

        Mockito.when(paymentServiceHandler.createPayment(Mockito.any()))
                .thenReturn(payment);
        Mockito.when(paymentProviderServiceHandler.createPayment(Mockito.any()))
                .thenReturn(createPaymentResponseDto);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentHandler.createPayment(createPaymentRequestDto));
    }

    @Test
    public void failCreatePaymentOnPaymentForTransactionAlreadyExists() {
//        Arrange
        long paymentMethodId = 1L;

        PaymentServiceCreatePaymentRequestDto createPaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        createPaymentRequestDto.setPaymentMethodId(paymentMethodId);

        PaymentMethod paymentMethod = getValidPaymentMethod();
        paymentMethod.setId(paymentMethodId);

        Payment payment = paymentMapper.toPayment(createPaymentRequestDto);
        payment.setPaymentMethod(paymentMethod);

        PaymentProviderServiceCreatePaymentResponseDto createPaymentResponseDto = getValidPaymentProviderServiceCreatePaymentResponseDto();
        createPaymentResponseDto.setPaymentStatus(PaymentStatus.COMPLETED);

        Mockito.when(paymentServiceHandler.createPayment(Mockito.any()))
                .thenThrow(EntityAlreadyExistsException.class);

//        Act & Assert
        Assertions.assertThrows(EntityAlreadyExistsException.class,
                () -> paymentHandler.createPayment(createPaymentRequestDto));
    }

    @Test
    public void failCreatePaymentOnPaymentMethodNotFound() {
//        Arrange
        long paymentMethodId = 1L;

        PaymentServiceCreatePaymentRequestDto createPaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        createPaymentRequestDto.setPaymentMethodId(paymentMethodId);

        PaymentMethod paymentMethod = getValidPaymentMethod();
        paymentMethod.setId(paymentMethodId);

        Payment payment = paymentMapper.toPayment(createPaymentRequestDto);
        payment.setPaymentMethod(paymentMethod);

        PaymentProviderServiceCreatePaymentResponseDto createPaymentResponseDto = getValidPaymentProviderServiceCreatePaymentResponseDto();
        createPaymentResponseDto.setPaymentStatus(PaymentStatus.COMPLETED);

        Mockito.when(paymentServiceHandler.createPayment(Mockito.any()))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> paymentHandler.createPayment(createPaymentRequestDto));
    }

    @Test
    public void failCreatePaymentOnPaymentProviderService4xxErrorResponse() {
//        Arrange
        long paymentMethodId = 1L;

        PaymentServiceCreatePaymentRequestDto createPaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        createPaymentRequestDto.setPaymentMethodId(paymentMethodId);

        PaymentMethod paymentMethod = getValidPaymentMethod();
        paymentMethod.setId(paymentMethodId);

        Payment payment = paymentMapper.toPayment(createPaymentRequestDto);
        payment.setPaymentMethod(paymentMethod);

        PaymentProviderServiceCreatePaymentResponseDto createPaymentResponseDto = getValidPaymentProviderServiceCreatePaymentResponseDto();
        createPaymentResponseDto.setPaymentStatus(PaymentStatus.COMPLETED);

        Mockito.when(paymentProviderServiceHandler.createPayment(Mockito.any()))
                .thenThrow(ClientHttpException.class);

//        Act & Assert
        Assertions.assertThrows(ClientHttpException.class,
                () -> paymentHandler.createPayment(createPaymentRequestDto));
    }

    @Test
    public void failCreatePaymentOnPaymentProviderService5xxErrorResponse() {
//        Arrange
        long paymentMethodId = 1L;

        PaymentServiceCreatePaymentRequestDto createPaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
        createPaymentRequestDto.setPaymentMethodId(paymentMethodId);

        PaymentMethod paymentMethod = getValidPaymentMethod();
        paymentMethod.setId(paymentMethodId);

        Payment payment = paymentMapper.toPayment(createPaymentRequestDto);
        payment.setPaymentMethod(paymentMethod);

        PaymentProviderServiceCreatePaymentResponseDto createPaymentResponseDto = getValidPaymentProviderServiceCreatePaymentResponseDto();
        createPaymentResponseDto.setPaymentStatus(PaymentStatus.COMPLETED);

        Mockito.when(paymentProviderServiceHandler.createPayment(Mockito.any()))
                .thenThrow(ServiceHttpException.class);

//        Act & Assert
        Assertions.assertThrows(ServiceHttpException.class,
                () -> paymentHandler.createPayment(createPaymentRequestDto));
    }
}
