package aq.project.services;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.entities.OutboxEvent;
import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.OutboxEventException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.mappers.OutboxEventMapper;
import aq.project.messages.TransactionRequest;
import aq.project.repositories.OutboxEventRepository;
import aq.project.util.request_handlers.DepositRequestHandler;
import aq.project.util.request_handlers.TransferRequestHandler;
import aq.project.util.request_handlers.WithdrawRequestHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static aq.project.util.RequestPropertyKeys.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final DepositRequestHandler depositRequestHandler;
    private final WithdrawRequestHandler withdrawRequestHandler;
    private final TransferRequestHandler transferRequestHandler;

    private final OutboxEventMapper outboxEventMapper;

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.wallet_operation_request.name}")
    public void handleTransactionRequest(ConsumerRecord<String, TransactionRequest> consumerRecord) throws WalletConstrainsException, NoSuchWalletException, CreditCardConstrainsException {
        Headers headers = consumerRecord.headers();
        TransactionRequest transactionRequest = consumerRecord.value();
        if(transactionRequest.getOperationType() == OperationType.WITHDRAW) {
            transactionRequest.putProperty(RECIPIENT_WALLET_ID, getPropertyFromHeader(headers, RECIPIENT_WALLET_ID));
            withdrawRequestHandler.handleRequestMessage(transactionRequest, outboxEventMapper, outboxEventRepository);
        } else if(transactionRequest.getOperationType() == OperationType.DEPOSIT) {
            transactionRequest.putProperty(RECIPIENT_WALLET_ID, getPropertyFromHeader(headers, RECIPIENT_WALLET_ID));
            depositRequestHandler.handleRequestMessage(transactionRequest, outboxEventMapper, outboxEventRepository);
        } else if(transactionRequest.getOperationType() == OperationType.TRANSFER) {
            transactionRequest.putProperty(SENDER_WALLET_ID, getPropertyFromHeader(headers, SENDER_WALLET_ID));
            transactionRequest.putProperty(RECIPIENT_WALLET_ID, getPropertyFromHeader(headers, RECIPIENT_WALLET_ID));
            transferRequestHandler.handleRequestMessage(transactionRequest, outboxEventMapper, outboxEventRepository);
        }
    }

    private String getPropertyFromHeader(Headers headers, String header) {
        byte[] bytes = headers.headers(header).iterator().next().value();
        return new String(bytes);
    }

    public TransactionStatus getTransactionStatus(String transactionId) throws OutboxEventException {
        OutboxEvent outboxEvent = outboxEventRepository.findById(transactionId)
                .orElseThrow(() -> new OutboxEventException(String.format("Transaction with id [%s] not found", transactionId)));
        return outboxEvent.getTransactionStatus();
    }
}
