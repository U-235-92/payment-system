package aq.project.test.services.deposit_transaction_service.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.services.DepositTransactionService;
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

import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.*;

@ExtendWith(MockitoExtension.class)
public class FailTransactionUnitTest {

    @Spy
    private PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;
    @Spy
    private WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;

    @Mock
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    @Mock
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;
    @Mock
    private WalletServiceDepositTransactionRequestRepository walletServiceDepositTransactionRequestRepository;

    @InjectMocks
    private DepositTransactionService depositTransactionService;

    @BeforeEach
    public void setUpMerchantId() {
        ReflectionTestUtils.setField(depositTransactionService, "merchantId", "test-merchant-id");
    }

    @Test
    public void successFailTransaction() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.failTransaction(transactionId));

        Mockito.verify(paymentProviderServiceTransactionRequestRepository, Mockito.times(1))
                .save(Mockito.any(PaymentProviderServiceTransactionRequest.class));
        Mockito.verify(walletServiceDepositTransactionRequestRepository, Mockito.times(1))
                .save(Mockito.any(WalletServiceDepositTransactionRequest.class));
    }

    @Test
    public void failFailTransactionOnTransactionServiceDepositTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> depositTransactionService.failTransaction(transactionId));

        Mockito.verify(paymentProviderServiceTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(PaymentProviderServiceTransactionRequest.class));
        Mockito.verify(walletServiceDepositTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(WalletServiceDepositTransactionRequest.class));
    }

    @Test
    public void failFailTransactionOnInvalidTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));

//        Act & Assert
        Assertions.assertThrows(ProcessOperationException.class,
                () -> depositTransactionService.failTransaction(transactionId));

        Mockito.verify(paymentProviderServiceTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(PaymentProviderServiceTransactionRequest.class));
        Mockito.verify(walletServiceDepositTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(WalletServiceDepositTransactionRequest.class));
    }
}
