package aq.project.test.services.withdraw_transaction_service.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceWithdrawTransactionRequestRepository;
import aq.project.services.WithdrawTransactionService;
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

import static aq.project._utils.entities.transaction_service.WithdrawTransactionServiceEntities.getValidTransactionServiceWithdrawTransaction;

@ExtendWith(MockitoExtension.class)
public class FailTransactionUnitTest {

    @Spy
    private PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;
    @Spy
    private WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;

    @Mock
    private TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    @Mock
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;
    @Mock
    private WalletServiceWithdrawTransactionRequestRepository walletServiceWithdrawTransactionRequestRepository;

    @InjectMocks
    private WithdrawTransactionService withdrawTransactionService;

    @BeforeEach
    public void setUpMerchantId() {
        ReflectionTestUtils.setField(withdrawTransactionService, "merchantId", "test-merchant-id");
    }

    @Test
    public void successFailTransaction() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction = getValidTransactionServiceWithdrawTransaction();
        transactionServiceWithdrawTransaction.setId(transactionId);
        transactionServiceWithdrawTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceWithdrawTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.failTransaction(transactionId));

        Mockito.verify(paymentProviderServiceTransactionRequestRepository, Mockito.times(1))
                .save(Mockito.any(PaymentProviderServiceTransactionRequest.class));
        Mockito.verify(walletServiceWithdrawTransactionRequestRepository, Mockito.times(1))
                .save(Mockito.any(WalletServiceWithdrawTransactionRequest.class));
    }

    @Test
    public void failFailTransactionOnTransactionServiceWithdrawTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> withdrawTransactionService.failTransaction(transactionId));

        Mockito.verify(paymentProviderServiceTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(PaymentProviderServiceTransactionRequest.class));
        Mockito.verify(walletServiceWithdrawTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(WalletServiceWithdrawTransactionRequest.class));
    }

    @Test
    public void failFailTransactionOnInvalidTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction = getValidTransactionServiceWithdrawTransaction();
        transactionServiceWithdrawTransaction.setId(transactionId);
        transactionServiceWithdrawTransaction.setStatus(TransactionStatus.FAILED);

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceWithdrawTransaction));

//        Act & Assert
        Assertions.assertThrows(ProcessOperationException.class,
                () -> withdrawTransactionService.failTransaction(transactionId));

        Mockito.verify(paymentProviderServiceTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(PaymentProviderServiceTransactionRequest.class));
        Mockito.verify(walletServiceWithdrawTransactionRequestRepository, Mockito.never())
                .save(Mockito.any(WalletServiceWithdrawTransactionRequest.class));
    }
}
