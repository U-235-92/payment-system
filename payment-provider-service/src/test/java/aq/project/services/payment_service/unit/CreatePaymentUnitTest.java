package aq.project.services.payment_service.unit;

import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.entities.Payment;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantException;
import aq.project.repositories.PaymentRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.services.PaymentService;
import aq.project.utils.mappers.PaymentMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.entities.PaymentServiceEntities.*;

@ExtendWith(MockitoExtension.class)
public class CreatePaymentUnitTest {

    private static final String TEST_MERCHANT_ID = "test-merchant-id";

    @Spy
    private PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    public void setPaymentServiceFieldsUp() {
        ReflectionTestUtils.setField(paymentService, "getTransactionRetryPeriod", 1500L);
        ReflectionTestUtils.setField(paymentService, "maxGetTransactionRetryCount", 3);
    }

    @BeforeEach
    void setUpSecurity() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getPrincipal()).thenReturn(TEST_MERCHANT_ID);
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDownSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void successCreatePayment() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(transactionId);
        paymentProviderServiceCreatePaymentRequestDto.setMerchantId(TEST_MERCHANT_ID);

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
        paymentProviderServiceCreatePaymentRequestDto.setMerchantId(TEST_MERCHANT_ID);

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);

        Payment payment = getValidPayment();
        payment.setTransaction(transaction);

        Mockito.when(paymentRepository.findByTransactionId(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(payment));

//        Act & Assert
        Assertions.assertThrows(EntityAlreadyExistsException.class,
                () -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto));

        Mockito.verify(paymentRepository, Mockito.never()).save(Mockito.any(Payment.class));
    }

    @Test
    public void failCreatePaymentOnTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(transactionId);
        paymentProviderServiceCreatePaymentRequestDto.setMerchantId(TEST_MERCHANT_ID);

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

    @Test
    public void failCreatePaymentOnNotEqualsMerchantId() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(transactionId);
        paymentProviderServiceCreatePaymentRequestDto.setMerchantId("another-test-merchant-id");

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);

        Payment payment = getValidPayment();
        payment.setTransaction(transaction);

//        Act & Assert
        Assertions.assertThrows(ForeignMerchantException.class,
                () -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto));
    }
}
