package aq.project.test.handlers.payment_provider_service_handler;

import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.payment_provider_service.PaymentApiClient;
import aq.project.utils.handlers.PaymentProviderServiceHandler;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static aq.project._utils.entities.PaymentServiceEntities.*;

@ExtendWith(MockitoExtension.class)
public class CreatePaymentUnitTest {

    @Spy
    private PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    @Mock
    private PaymentApiClient paymentApiClient;

    @InjectMocks
    private PaymentProviderServiceHandler paymentProviderServiceHandler;

    @BeforeEach
    public void setUpMerchantId() {
        ReflectionTestUtils.setField(paymentProviderServiceHandler, "merchantId", "test-merchant-id");
        ReflectionTestUtils.setField(paymentProviderServiceHandler, "merchantSecret", "test-merchant-secret");
        ReflectionTestUtils.setField(paymentProviderServiceHandler, "paymentProviderServiceName", "test-service-name");
    }

    @Test
    public void successCreatePayment() {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        Long paymentMethodId = 1L;

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

        Mockito.when(paymentApiClient.createPayment(
                        Mockito.any(String.class),
                        Mockito.any(PaymentProviderServiceCreatePaymentRequestDto.class),
                        Mockito.any(String.class)))
                .thenReturn(ResponseEntity.ok(paymentProviderServiceCreatePaymentResponseDto));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceHandler.createPayment(paymentServiceCreatePaymentRequestDto));
    }
}
