package aq.project.test.services.deposit_transaction_service.unit;

import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.services.DepositTransactionService;
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

import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;

@ExtendWith(MockitoExtension.class)
public class GetTransactionStatusUnitTest {

    @Spy
    private PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    @Mock
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    @Mock
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Mock
    private Fallback fallback;

    @InjectMocks
    private DepositTransactionService depositTransactionService;

    @Test
    public void successGetTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(transactionServiceDepositTransactionRepository.findById(transactionId))
                .thenReturn(java.util.Optional.of(getValidTransactionServiceDepositTransaction()));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOnEntityNotFoundException() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(transactionServiceDepositTransactionRepository.findById(transactionId))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> depositTransactionService.getTransactionStatus(transactionId));
    }
}
