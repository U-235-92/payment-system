package aq.project.get_transaction_info.unit;

import aq.project.dto.TransactionResponseDto;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TransactionService;
import aq.project.utils.mappers.TransactionMapper;
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
import static aq.project._utils.Entities.getValidPendingTransaction;

@ExtendWith(MockitoExtension.class)
public class GetTransactionInfoUnitTest {

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void successGetTransactionInfoUnitTest() {
//        Arrange
        Transaction transaction = getValidPendingTransaction();
        UUID transactionId = transaction.getId();
        String merchantId = getValidMerchant().getId();

        Mockito.when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

//        Act
        TransactionResponseDto responseDto = transactionService.getTransactionInfo(transactionId, merchantId);

//        Assert
        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(responseDto, transactionMapper.toTransactionResponseDto(transaction));
        Mockito.verify(transactionRepository, Mockito.times(1)).findById(transactionId);
    }

    @Test
    public void failGetTransactionInfoOnUnknownTransactionIdUnitTest() {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        String merchantId = "another-merchant-service";

        Mockito.when(transactionRepository.findById(transactionId)).thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transactionService.getTransactionInfo(transactionId, merchantId));
    }

    @Test
    public void failGetTransactionInfoOnTransactionBelongsAnotherMerchantUnitTest() {
//        Arrange
        Transaction transaction = getValidPendingTransaction();
        UUID transactionId = transaction.getId();
        String merchantId = "another-merchant-service";

        Mockito.when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

//        Act & Assert
        Assertions.assertThrows(ForeignMerchantTransactionException.class,
                () -> transactionService.getTransactionInfo(transactionId, merchantId));
    }
}
