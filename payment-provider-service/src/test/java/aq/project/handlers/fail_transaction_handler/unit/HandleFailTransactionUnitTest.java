package aq.project.handlers.fail_transaction_handler.unit;

import aq.project.dto.PaymentProviderServiceFailTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.FailTransactionHandler;
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

import static aq.project._utils.entities.FailTransactionHandlerEntities.*;

@ExtendWith(MockitoExtension.class)
public class HandleFailTransactionUnitTest {

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
    private FailTransactionHandler failTransactionHandler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(failTransactionHandler, "serviceName", "serviceName");
    }

    @Test
    public void successHandleFailTransaction() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        PaymentProviderServiceFailTransactionRequestDto requestDto =
                getValidPaymentProviderServiceFailTransactionRequestDto();
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
        Assertions.assertDoesNotThrow(() -> failTransactionHandler.handleFailTransaction(requestDto));
        Assertions.assertEquals(TransactionStatus.MARKED_FAILED, transaction.getStatus());

        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failHandleFailTransactionOnInvalidPaymentProviderServiceFailTransactionRequestDto() {
//        Arrange
        PaymentProviderServiceFailTransactionRequestDto requestDto =
                getInvalidPaymentProviderServiceFailTransactionRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> failTransactionHandler.handleFailTransaction(requestDto));
    }

    @Test
    public void failHandleFailTransactionOnMerchantDoesntExist() {
//        Arrange
        PaymentProviderServiceFailTransactionRequestDto requestDto =
                getValidPaymentProviderServiceFailTransactionRequestDto();

        Transaction transaction = getValidTransaction();
        transaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transaction));
        Mockito.when(merchantRepository.existsById(Mockito.any()))
                .thenReturn(false);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> failTransactionHandler.handleFailTransaction(requestDto));
        Assertions.assertEquals(TransactionStatus.PENDING, transaction.getStatus());
    }

    @Test
    public void failHandleFailTransactionOnForeignMerchantTransactionException() {
//        Arrange
        Merchant merchantA = getValidMerchant();
        merchantA.setId("merchant-A");

        Merchant merchantB = getValidMerchant();
        merchantB.setId("merchant-B");

        PaymentProviderServiceFailTransactionRequestDto requestDto =
                getValidPaymentProviderServiceFailTransactionRequestDto();
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
                () -> failTransactionHandler.handleFailTransaction(requestDto));
        Assertions.assertEquals(TransactionStatus.PENDING, transaction.getStatus());
    }

    @Test
    public void failHandleFailTransactionOnTransactionInNotPendingOrCompletedStatus() {
//        Arrange
        String merchantId = UUID.randomUUID().toString();

        PaymentProviderServiceFailTransactionRequestDto requestDto =
                getValidPaymentProviderServiceFailTransactionRequestDto();
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
                () -> failTransactionHandler.handleFailTransaction(requestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transaction.getStatus());
    }
}
