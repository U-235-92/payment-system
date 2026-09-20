package aq.project.test.services.withdraw_transaction_service.unit;

import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.services.WithdrawTransactionService;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.resilence.Fallback;
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

import static aq.project._utils.entities.transaction_service.WithdrawTransactionServiceEntities.getValidTransactionServiceWithdrawTransaction;

@ExtendWith(MockitoExtension.class)
public class CreateTransactionUnitTest {

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

    @BeforeEach
    public void setUpMerchantId() {
        ReflectionTestUtils.setField(withdrawTransactionService, "merchantId", "test-merchant-id");
    }

    @Test
    public void successCreateTransaction() {
//        Arrange
        TransactionServiceWithdrawTransaction transaction = getValidTransactionServiceWithdrawTransaction();

        Mockito.doReturn(getValidTransactionServiceWithdrawTransaction())
                .when(transactionServiceWithdrawTransactionRepository)
                .save(Mockito.any());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.createTransaction(transaction));

        Mockito.verify(transactionServiceWithdrawTransactionRepository).save(Mockito.any());
        Mockito.verify(paymentProviderServiceTransactionRequestRepository).save(Mockito.any());
    }
}
