package aq.project.test.services.transfer_transaction_service.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceTransferTransactionRequestRepository;
import aq.project.services.TransferTransactionService;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.mappers.WalletServiceTransactionDtoMapper;
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

import static aq.project._utils.entities.transaction_service.TransferTransactionServiceEntities.getValidTransactionServiceTransferTransaction;

@ExtendWith(MockitoExtension.class)
public class FailTransactionUnitTest {

    @Spy
    private PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;
    @Spy
    private WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;

    @Mock
    private TransactionServiceTransferTransactionRepository transactionServiceTransferTransactionRepository;
    @Mock
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;
    @Mock
    private WalletServiceTransferTransactionRequestRepository walletServiceTransferTransactionRequestRepository;

    @InjectMocks
    private TransferTransactionService transferTransactionService;

    @BeforeEach
    public void setUpMerchantId() {
        ReflectionTestUtils.setField(transferTransactionService, "merchantId", "test-merchant-id");
    }

    @Test
    public void successFailTransaction() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceTransferTransaction transactionServiceTransferTransaction = getValidTransactionServiceTransferTransaction();
        transactionServiceTransferTransaction.setId(transactionId);
        transactionServiceTransferTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionServiceTransferTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceTransferTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.failTransaction(transactionId));

        Mockito.verify(paymentProviderServiceTransactionRequestRepository, Mockito.times(1))
                .save(Mockito.any(PaymentProviderServiceTransactionRequest.class));
        Mockito.verify(walletServiceTransferTransactionRequestRepository, Mockito.times(1))
                .save(Mockito.any(WalletServiceTransferTransactionRequest.class));
    }

    @Test
    public void failFailTransactionOnTransactionServiceTransferTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(transactionServiceTransferTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transferTransactionService.failTransaction(transactionId));

        Mockito.verify(paymentProviderServiceTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(PaymentProviderServiceTransactionRequest.class));
        Mockito.verify(walletServiceTransferTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(WalletServiceTransferTransactionRequest.class));
    }

    @Test
    public void failFailTransactionOnInvalidTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceTransferTransaction transactionServiceTransferTransaction = getValidTransactionServiceTransferTransaction();
        transactionServiceTransferTransaction.setId(transactionId);
        transactionServiceTransferTransaction.setStatus(TransactionStatus.FAILED);

        Mockito.when(transactionServiceTransferTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceTransferTransaction));

//        Act & Assert
        Assertions.assertThrows(ProcessOperationException.class,
                () -> transferTransactionService.failTransaction(transactionId));

        Mockito.verify(paymentProviderServiceTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(PaymentProviderServiceTransactionRequest.class));
        Mockito.verify(walletServiceTransferTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(WalletServiceTransferTransactionRequest.class));
    }
}
