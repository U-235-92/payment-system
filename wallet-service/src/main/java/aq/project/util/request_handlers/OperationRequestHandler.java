package aq.project.util.request_handlers;

import aq.project.dto.TransactionStatus;
import aq.project.entities.OutboxEvent;
import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.mappers.OutboxEventMapper;
import aq.project.messages.TransactionRequest;
import aq.project.repositories.OutboxEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class OperationRequestHandler {

    protected OperationRequestHandler() {
        super();
    }

    @Transactional
    public final void handleRequestMessage(TransactionRequest transactionRequest,
                                           OutboxEventMapper outboxEventMapper,
                                           OutboxEventRepository outboxEventRepository
    ) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        try {
            checkRequestMessagePropertyConstrains(transactionRequest);
            handleRequestMessageOperation(transactionRequest);
            commitOutboxEvent(outboxEventRepository, outboxEventMapper.toOutboxEvent(transactionRequest), TransactionStatus.COMPLETED);
        } catch (WalletConstrainsException | CreditCardConstrainsException | NoSuchWalletException e) {
            commitOutboxEvent(outboxEventRepository, outboxEventMapper.toOutboxEvent(transactionRequest), TransactionStatus.FAILED);
            throw e;
        }
    }

    private void commitOutboxEvent(OutboxEventRepository outboxEventRepository,
                                   OutboxEvent outboxEvent,
                                   TransactionStatus transactionStatus
    ) {
        outboxEvent.setTransactionStatus(transactionStatus);
        outboxEventRepository.save(outboxEvent);
    }

    protected void checkRequestMessagePropertyConstrains(TransactionRequest transactionRequest) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        throw new UnsupportedOperationException();
    }

    protected void handleRequestMessageOperation(TransactionRequest transactionRequest) {
        throw new UnsupportedOperationException();
    }
}
