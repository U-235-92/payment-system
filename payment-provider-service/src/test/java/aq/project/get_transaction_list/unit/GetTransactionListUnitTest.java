package aq.project.get_transaction_list.unit;

import aq.project.dto.TransactionResponsePaymentProviderServiceDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static aq.project._utils.Entities.*;

@ExtendWith(MockitoExtension.class)
public class GetTransactionListUnitTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void successGetTransactionListUnitTest() {
//        Arrange
        Transaction transaction = getValidPendingTransaction();
        Merchant merchant = getValidMerchant();
        String merchantId = merchant.getId();

        Mockito.when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        Mockito.when(transactionRepository.findByMerchantIdAndMetadataCreatedAtBetween(
                        Mockito.eq(merchantId), Mockito.any(OffsetDateTime.class), Mockito.any(OffsetDateTime.class)))
                .thenReturn(List.of(transaction));
//        Act
        List<TransactionResponsePaymentProviderServiceDto> dtoList = transactionService.getTransactionList(
                OffsetDateTime.now(), OffsetDateTime.now().plusHours(1L), merchantId);

//        Assert
        Assertions.assertFalse(dtoList.isEmpty());
        Mockito.verify(merchantRepository, Mockito.times(1))
                .findById(merchantId);
        Mockito.verify(transactionRepository, Mockito.times(1))
                .findByMerchantIdAndMetadataCreatedAtBetween(
                        Mockito.eq(merchantId), Mockito.any(OffsetDateTime.class), Mockito.any(OffsetDateTime.class));
    }

    @Test
    public void failGetTransactionListOnUnknownMerchantIdUnitTest() {
//        Arrange
        String merchantId = getValidMerchantId();

        Mockito.when(merchantRepository.findById(Mockito.anyString())).thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transactionService.getTransactionList(
                        OffsetDateTime.now(), OffsetDateTime.now().plusHours(1L), merchantId));
    }
}