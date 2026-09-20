package aq.project.test.services.transfer_transaction_service.unit;

import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.services.TransferTransactionService;
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

import static aq.project._utils.entities.transaction_service.TransferTransactionServiceEntities.getValidTransactionServiceTransferTransaction;

@ExtendWith(MockitoExtension.class)
public class CreateTransactionUnitTest {

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

    @BeforeEach
    public void setUpMerchantId() {
        ReflectionTestUtils.setField(transferTransactionService, "merchantId", "test-merchant-id");
    }

    @Test
    public void successCreateTransaction() {
//        Arrange
        TransactionServiceTransferTransaction transaction = getValidTransactionServiceTransferTransaction();

        Mockito.doReturn(getValidTransactionServiceTransferTransaction())
                .when(transactionServiceTransferTransactionRepository)
                .save(Mockito.any());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.createTransaction(transaction));

        Mockito.verify(transactionServiceTransferTransactionRepository).save(Mockito.any());
        Mockito.verify(paymentProviderServiceTransactionRequestRepository).save(Mockito.any());
    }
}
