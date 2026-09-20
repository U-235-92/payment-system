package aq.project.test.services.deposit_transaction_service.unit;

import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.services.DepositTransactionService;
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

import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;

@ExtendWith(MockitoExtension.class)
public class CreateTransactionUnitTest {

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

    @BeforeEach
    public void setUpMerchantId() {
        ReflectionTestUtils.setField(depositTransactionService, "merchantId", "test-merchant-id");
    }

    @Test
    public void successCreateTransaction() {
//        Arrange
        TransactionServiceDepositTransaction transaction = getValidTransactionServiceDepositTransaction();

        Mockito.doReturn(getValidTransactionServiceDepositTransaction())
                .when(transactionServiceDepositTransactionRepository)
                .save(Mockito.any());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.createTransaction(transaction));

        Mockito.verify(transactionServiceDepositTransactionRepository).save(Mockito.any());
        Mockito.verify(paymentProviderServiceTransactionRequestRepository).save(Mockito.any());
    }
}
