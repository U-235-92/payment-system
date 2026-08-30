package aq.project.services;

import aq.project.dto.*;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.TransactionHandler;
import aq.project.utils.mappers.TransactionMapper;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;

    private final TransactionHandler transactionHandler;

    private final TraceContext traceContext;

    @KafkaListener(topics = "${service.kafka.topics.create_transaction_request.name}")
    public void handleCreateTransaction(
            CreateTransactionRequestDto requestDto
    ) {
        try {
            Transaction transaction = transactionMapper.toTransaction(requestDto);

            String merchantId = requestDto.getMerchantId();
            String traceId = requestDto.getTraceId();

            setTraceId(traceId);

            transactionHandler.handleCreateTransaction(transaction, merchantId);
        } finally {
            traceContext.clean();
        }
    }

    @KafkaListener(topics = "${service.kafka.topics.fail_transaction_request.name}")
    public void handleFailTransaction(FailTransactionRequestDto requestDto) {
        try {
            UUID transactionId = requestDto.getTransactionId();

            String merchantId = requestDto.getMerchantId();
            String traceId = requestDto.getTraceId();

            setTraceId(traceId);

            transactionHandler.handleFailTransaction(transactionId, merchantId);
        } finally {
            traceContext.clean();
        }
    }

    @KafkaListener(topics = "${service.kafka.topics.cancel_transaction_request.name}")
    public void handleCancelTransaction(CancelTransactionRequestDto requestDto) {
        try {
            UUID transactionId = requestDto.getTransactionId();

            String merchantId = requestDto.getMerchantId();
            String traceId = requestDto.getTraceId();

            setTraceId(traceId);

            transactionHandler.handleCancelTransaction(transactionId, merchantId);
        } finally {
            traceContext.clean();
        }
    }

    private void setTraceId(String traceId) {
        traceContext.clean();
        traceContext.setTraceId(traceId);
    }

    @Transactional(readOnly = true)
    public TransactionResponseDto getTransactionInfo(
            UUID transactionId,
            String merchantId
    ) {
        Transaction transaction = transactionHandler.getTransaction(transactionId);
        if(!transaction.getMerchant().getId().equals(merchantId))
            throw new ForeignMerchantTransactionException(
                    String.format("Transaction with id [%s] belongs to another merchant", transactionId));

        return transactionMapper.toTransactionResponseDto(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionList(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String merchantId
    ) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Merchant with id [%s] not found", merchantId)));

        List<Transaction> transactions = transactionRepository
                .findByMerchantIdAndMetadataCreatedAtBetween(merchant.getId(), startDate, endDate);

        return transactions.stream()
                .map(transactionMapper::toTransactionResponseDto)
                .collect(Collectors.toList());
    }
}