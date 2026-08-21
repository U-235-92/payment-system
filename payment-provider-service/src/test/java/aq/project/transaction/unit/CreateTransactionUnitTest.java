package aq.project.transaction.unit;

import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionResponseDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.MerchantRepository;
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

import static aq.project._utils.Entities.getValidMerchant;
import static aq.project._utils.Entities.getValidTransactionRequestDto;

@ExtendWith(MockitoExtension.class)
public class CreateTransactionUnitTest {

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void successCreateTransactionUnitTest() {
//        Arrange
        Merchant merchant = getValidMerchant();
        String merchantId = merchant.getId();
        TransactionRequestDto requestDto = getValidTransactionRequestDto();
        Transaction transaction = transactionMapper.toTransaction(requestDto);
        transaction.setMerchant(merchant);

        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);
        Mockito.when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));

//        Act
        TransactionResponseDto responseDto = transactionService.createTransaction(requestDto, merchantId);

//        Assert
        Mockito.verify(merchantRepository, Mockito.times(1)).findById(merchantId);
        Mockito.verify(transactionRepository, Mockito.times(1)).save(Mockito.any(Transaction.class));
        Assertions.assertEquals(transactionMapper.toTransactionResponseDto(transaction), responseDto);
    }

    @Test
    public void failCreateTransactionUnitOnUnknownMerchantIdTest() {
//        Arrange
        Merchant merchant = getValidMerchant();
        String merchantId = "unknown-service";
        TransactionRequestDto requestDto = getValidTransactionRequestDto();
        Transaction transaction = transactionMapper.toTransaction(requestDto);
        transaction.setMerchant(merchant);

        Mockito.when(merchantRepository.findById(merchantId)).thenThrow(EntityNotFoundException.class);

//        Act
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transactionService.createTransaction(requestDto, merchantId));

//        Assert
        Mockito.verify(merchantRepository, Mockito.times(1)).findById(merchantId);
        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any(Transaction.class));
    }
}
