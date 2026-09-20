package aq.project.test.services.transfer_transaction_service.unit;

import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.services.TransferTransactionService;
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

import static aq.project._utils.entities.transaction_service.TransferTransactionServiceEntities.getValidTransactionServiceTransferTransaction;

@ExtendWith(MockitoExtension.class)
public class GetTransactionStatusUnitTest {

    @Spy
    private PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    @Mock
    private TransactionServiceTransferTransactionRepository transactionServiceTransferTransactionRepository;
    @Mock
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @Mock
    private Fallback fallback;

    @InjectMocks
    private TransferTransactionService transferTransactionService;

    @Test
    public void successGetTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(transactionServiceTransferTransactionRepository.findById(transactionId))
                .thenReturn(java.util.Optional.of(getValidTransactionServiceTransferTransaction()));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOnEntityNotFoundException() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(transactionServiceTransferTransactionRepository.findById(transactionId))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transferTransactionService.getTransactionStatus(transactionId));
    }
}
