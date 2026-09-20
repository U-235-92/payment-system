package aq.project.test.services.withdraw_transaction_service.unit;

import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.services.WithdrawTransactionService;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.resilence.Fallback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static aq.project._utils.entities.transaction_service.WithdrawTransactionServiceEntities.getValidTransactionServiceWithdrawTransaction;

@ExtendWith(MockitoExtension.class)
public class GetTransactionStatusUnitTest {

    @Spy
    private PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    @Mock
    private TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    @Mock
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Mock
    private Fallback fallback;

    @InjectMocks
    private WithdrawTransactionService withdrawTransactionService;

    @Test
    public void successGetTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(transactionId))
                .thenReturn(java.util.Optional.of(getValidTransactionServiceWithdrawTransaction()));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOnEntityNotFoundException() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(transactionId))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> withdrawTransactionService.getTransactionStatus(transactionId));
    }
}
