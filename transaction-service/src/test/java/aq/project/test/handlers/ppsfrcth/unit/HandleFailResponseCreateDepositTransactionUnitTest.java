package aq.project.test.handlers.ppsfrcth.unit;

import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceErrorHandleTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.utils.handlers.payment_provider_service.response.PaymentProviderServiceFailResponseCreateTransactionHandler;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import io.opentelemetry.api.OpenTelemetry;
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

import static aq.project._utils.entities.payment_provider_service.PaymentProviderServiceEntities.getValidPaymentProviderServiceErrorHandleTransactionDto;
import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;

@ExtendWith(MockitoExtension.class)
public class HandleFailResponseCreateDepositTransactionUnitTest {

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Mock
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @InjectMocks
    private PaymentProviderServiceFailResponseCreateTransactionHandler paymentProviderServiceFailResponseCreateTransactionHandler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(paymentProviderServiceFailResponseCreateTransactionHandler, "serviceName", "service-name");
        ReflectionTestUtils.setField(paymentProviderServiceFailResponseCreateTransactionHandler, "paymentProviderServiceName", "payment-provider-service-name");
    }

    @Test
    public void successHandleFailResponseCreateTransactionOnPaymentProviderService() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceErrorHandleTransactionDto errorHandleTransactionDto = getValidPaymentProviderServiceErrorHandleTransactionDto();
        errorHandleTransactionDto.setTransactionId(transactionId);
        errorHandleTransactionDto.setOperation(Operation.DEPOSIT);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceFailResponseCreateTransactionHandler
                        .handleFailResponseCreateTransactionOnPaymentProviderService(
                                errorHandleTransactionDto));
        Assertions.assertEquals(transactionServiceDepositTransaction.getStatus().getValue(), TransactionStatus.FAILED.getValue());

        Mockito.verify(transactionServiceDepositTransactionRepository, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnPaymentProviderServiceWhenTransactionServiceDepositTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceErrorHandleTransactionDto errorHandleTransactionDto = getValidPaymentProviderServiceErrorHandleTransactionDto();
        errorHandleTransactionDto.setTransactionId(transactionId);
        errorHandleTransactionDto.setOperation(Operation.DEPOSIT);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceFailResponseCreateTransactionHandler
                        .handleFailResponseCreateTransactionOnPaymentProviderService(
                                errorHandleTransactionDto));

        Mockito.verify(transactionServiceDepositTransactionRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnPaymentProviderServiceWhenTransactionServiceDepositTransactionStatusIsFailed() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceErrorHandleTransactionDto errorHandleTransactionDto = getValidPaymentProviderServiceErrorHandleTransactionDto();
        errorHandleTransactionDto.setTransactionId(transactionId);
        errorHandleTransactionDto.setOperation(Operation.DEPOSIT);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceFailResponseCreateTransactionHandler
                        .handleFailResponseCreateTransactionOnPaymentProviderService(
                                errorHandleTransactionDto));

        Assertions.assertEquals(transactionServiceDepositTransaction.getStatus().getValue(), TransactionStatus.FAILED.getValue());
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnPaymentProviderServiceWhenTransactionServiceDepositTransactionStatusIsCancelled() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceErrorHandleTransactionDto errorHandleTransactionDto = getValidPaymentProviderServiceErrorHandleTransactionDto();
        errorHandleTransactionDto.setTransactionId(transactionId);
        errorHandleTransactionDto.setOperation(Operation.DEPOSIT);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.CANCELED);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceFailResponseCreateTransactionHandler
                        .handleFailResponseCreateTransactionOnPaymentProviderService(
                                errorHandleTransactionDto));

        Assertions.assertEquals(transactionServiceDepositTransaction.getStatus().getValue(), TransactionStatus.FAILED.getValue());
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnPaymentProviderServiceWhenTransactionServiceDepositTransactionStatusIsCompleted() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceErrorHandleTransactionDto errorHandleTransactionDto = getValidPaymentProviderServiceErrorHandleTransactionDto();
        errorHandleTransactionDto.setTransactionId(transactionId);
        errorHandleTransactionDto.setOperation(Operation.DEPOSIT);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.COMPLETED);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceFailResponseCreateTransactionHandler
                        .handleFailResponseCreateTransactionOnPaymentProviderService(
                                errorHandleTransactionDto));

        Assertions.assertEquals(transactionServiceDepositTransaction.getStatus().getValue(), TransactionStatus.FAILED.getValue());
    }
}
