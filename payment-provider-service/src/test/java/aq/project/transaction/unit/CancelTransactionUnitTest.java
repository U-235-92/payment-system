package aq.project.transaction.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.Entities.getValidMerchant;
import static aq.project._utils.Entities.getValidTransaction;

@ExtendWith(MockitoExtension.class)
public class CancelTransactionUnitTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void successCancelTransactionUnitTest() {
//        Arrange
        Transaction transaction = getValidTransaction();
        UUID transactionId = transaction.getId();
        String merchantId = getValidMerchant().getId();
        TransactionStatus transactionStatus = TransactionStatus.FAILED;

        Mockito.when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

//        Act
        transactionService.cancelTransaction(transactionId, merchantId, transactionStatus);

//        Assert
        Assertions.assertEquals(transaction.getStatus().getValue(), TransactionStatus.FAILED.getValue());
        Mockito.verify(transactionRepository, Mockito.times(1)).findById(transactionId);
    }

    @Test
    public void failCancelTransactionOnUnknownTransactionIdUnitTest() {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        String merchantId = "another-merchant-service";
        TransactionStatus transactionStatus = TransactionStatus.FAILED;

        Mockito.when(transactionRepository.findById(transactionId)).thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transactionService.cancelTransaction(transactionId, merchantId, transactionStatus));
    }

    @Test
    public void failCancelTransactionOnTransactionBelongsAnotherMerchantUnitTest() {
//        Arrange
        Transaction transaction = getValidTransaction();
        UUID transactionId = transaction.getId();
        String merchantId = "another-merchant-service";
        TransactionStatus transactionStatus = TransactionStatus.FAILED;

        Mockito.when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

//        Act & Assert
        Assertions.assertThrows(ForeignMerchantTransactionException.class,
                () -> transactionService.cancelTransaction(transactionId, merchantId, transactionStatus));
    }
}
