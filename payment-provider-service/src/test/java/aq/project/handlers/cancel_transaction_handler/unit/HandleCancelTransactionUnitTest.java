package aq.project.handlers.cancel_transaction_handler.unit;

import aq.project.dto.PaymentProviderServiceCancelTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.CancelTransactionHandler;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
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

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.entities.CancelTransactionHandlerEntities.*;

@ExtendWith(MockitoExtension.class)
public class HandleCancelTransactionUnitTest {

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @InjectMocks
    private CancelTransactionHandler cancelTransactionHandler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(cancelTransactionHandler, "serviceName", "serviceName");
    }

    @Test
    public void successHandleCancelTransaction() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        PaymentProviderServiceCancelTransactionRequestDto requestDto =
                getValidPaymentProviderServiceCancelTransactionRequestDto();
        requestDto.setMerchantId(merchantId);

        Merchant merchant = getValidMerchant();
        merchant.setId(merchantId);

        Transaction transaction = getValidTransaction();
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setMerchant(merchant);

        Mockito.when(transactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transaction));
        Mockito.when(merchantRepository.existsById(Mockito.any()))
                .thenReturn(true);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionHandler.handleCancelTransaction(requestDto));
        Assertions.assertEquals(TransactionStatus.MARKED_CANCELED, transaction.getStatus());

        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failHandleCancelTransactionOnInvalidPaymentProviderServiceCancelTransactionRequestDto() {
//        Arrange
        PaymentProviderServiceCancelTransactionRequestDto requestDto =
                getInvalidPaymentProviderServiceCancelTransactionRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> cancelTransactionHandler.handleCancelTransaction(requestDto));
    }

    @Test
    public void failHandleCancelTransactionOnMerchantDoesntExist() {
//        Arrange
        PaymentProviderServiceCancelTransactionRequestDto requestDto =
                getValidPaymentProviderServiceCancelTransactionRequestDto();

        Transaction transaction = getValidTransaction();
        transaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transaction));
        Mockito.when(merchantRepository.existsById(Mockito.any()))
                .thenReturn(false);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> cancelTransactionHandler.handleCancelTransaction(requestDto));
        Assertions.assertEquals(TransactionStatus.PENDING, transaction.getStatus());
    }

    @Test
    public void failHandleCancelTransactionOnForeignMerchantTransactionException() {
//        Arrange
        Merchant merchantA = getValidMerchant();
        merchantA.setId("merchant-A");

        Merchant merchantB = getValidMerchant();
        merchantB.setId("merchant-B");

        PaymentProviderServiceCancelTransactionRequestDto requestDto =
                getValidPaymentProviderServiceCancelTransactionRequestDto();
        requestDto.setMerchantId(merchantA.getId());

        Transaction transaction = getValidTransaction();
        transaction.setMerchant(merchantB);
        transaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transaction));
        Mockito.when(merchantRepository.existsById(Mockito.any()))
                .thenReturn(true);

//        Act & Assert
        Assertions.assertThrows(ForeignMerchantTransactionException.class,
                () -> cancelTransactionHandler.handleCancelTransaction(requestDto));
        Assertions.assertEquals(TransactionStatus.PENDING, transaction.getStatus());
    }

    @Test
    public void failHandleCancelTransactionOnTransactionInNotPendingOrCompletedStatus() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        PaymentProviderServiceCancelTransactionRequestDto requestDto =
                getValidPaymentProviderServiceCancelTransactionRequestDto();
        requestDto.setMerchantId(merchantId);

        Merchant merchant = getValidMerchant();
        merchant.setId(merchantId);

        Transaction transaction = getValidTransaction();
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setMerchant(merchant);

        Mockito.when(transactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transaction));
        Mockito.when(merchantRepository.existsById(Mockito.any()))
                .thenReturn(true);

//        Act & Assert
        Assertions.assertThrows(ProhibitedOperationException.class,
                () -> cancelTransactionHandler.handleCancelTransaction(requestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transaction.getStatus());
    }
}
