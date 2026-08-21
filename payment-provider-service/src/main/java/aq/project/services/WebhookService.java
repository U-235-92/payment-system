package aq.project.services;

import aq.project.dto.TransactionStatus;
import aq.project.dto.TransactionStatusDto;
import aq.project.entities.Transaction;
import aq.project.entities.Webhook;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.TransactionRepository;
import aq.project.repositories.WebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final TransactionRepository transactionRepository;
    private final WebhookRepository webhookRepository;

    @Transactional
    public void updateTransactionStatus(TransactionStatusDto transactionStatusDto) {
        setUpTransactionStatus(transactionStatusDto);
    }

    private void setUpTransactionStatus(TransactionStatusDto transactionStatusDto) {
        UUID transactionId = UUID.fromString(transactionStatusDto.getId());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id [%s] not found", transactionId)));

        if(transaction.getStatus() == TransactionStatus.PENDING) {
            transaction.setStatus(transactionStatusDto.getStatus());
            saveWebhook(transactionStatusDto, transaction);
        }
    }

    private void saveWebhook(TransactionStatusDto transactionStatusDto, Transaction transaction) {
        Webhook webhook = new Webhook();
        webhook.setEventType(transactionStatusDto.getEventType());
        webhook.setTransaction(transaction);
        webhook.getPayload().put("description", transactionStatusDto.getDescription());
        webhook.setNotificationUrl(transactionStatusDto.getNotificationUrl());
        webhookRepository.save(webhook);
    }
}
