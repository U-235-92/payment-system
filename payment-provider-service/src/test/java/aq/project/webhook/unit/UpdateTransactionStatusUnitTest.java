package aq.project.webhook.unit;

import aq.project.dto.TransactionStatus;
import aq.project.dto.TransactionStatusDto;
import aq.project.entities.Transaction;
import aq.project.entities.Webhook;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.TransactionRepository;
import aq.project.repositories.WebhookRepository;
import aq.project.services.WebhookService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.Entities.*;

@ExtendWith(MockitoExtension.class)
public class UpdateTransactionStatusUnitTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private WebhookRepository webhookRepository;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    public void successUpdateTransactionStatusUnitTest() {
//        Arrange
        Transaction transaction = getValidTransaction();
        TransactionStatusDto transactionStatusDto = getValidTransactionStatusDto();

        Mockito.when(transactionRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(transaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> webhookService.updateTransactionStatus(transactionStatusDto));
        Mockito.verify(webhookRepository, Mockito.times(1)).save(Mockito.any(Webhook.class));
    }

    @Test
    public void successUpdateTransactionStatusOnRepeatableTransactionUnitTest() {
//        Arrange
        Transaction transaction = getValidTransaction();
        transaction.setStatus(TransactionStatus.COMPLETED);
        TransactionStatusDto transactionStatusDto = getValidTransactionStatusDto();

        Mockito.when(transactionRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(transaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> webhookService.updateTransactionStatus(transactionStatusDto));
        Mockito.verify(webhookRepository, Mockito.never()).save(Mockito.any(Webhook.class));
    }

    @Test
    public void successUpdateTransactionStatusOnCompletedTransactionUnitTest() {
//        Arrange
        Transaction transaction = getValidTransaction();
        transaction.setStatus(TransactionStatus.COMPLETED);

        TransactionStatusDto transactionStatusDto = getValidTransactionStatusDto();
        transactionStatusDto.setStatus(TransactionStatus.FAILED);

        Mockito.when(transactionRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(transaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> webhookService.updateTransactionStatus(transactionStatusDto));
        Mockito.verify(webhookRepository, Mockito.never()).save(Mockito.any(Webhook.class));
    }

    @Test
    public void failUpdateTransactionStatusOnUnknownTransactionIdUnitTest() {
//        Arrange
        TransactionStatusDto transactionStatusDto = getValidTransactionStatusDto();

        Mockito.when(transactionRepository.findById(Mockito.any(UUID.class))).thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> webhookService.updateTransactionStatus(transactionStatusDto));
    }
}
