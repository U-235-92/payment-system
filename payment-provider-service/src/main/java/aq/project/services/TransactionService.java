package aq.project.services;

import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionResponseDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;

    @Transactional
    public TransactionResponseDto createTransaction(
            TransactionRequestDto requestDto,
            String merchantId
    ) {
        Transaction transaction = transactionMapper.toTransaction(requestDto);

        if(transactionRepository.existsById(transaction.getId()))
            throw new EntityAlreadyExistsException(
                    String.format("Transaction with id: [%s] already exists",
                            transaction.getId()));

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Merchant with id [%s] not found", merchantId)));

        transaction.setMerchant(merchant);

        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toTransactionResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public TransactionResponseDto getTransactionInfo(
            UUID transactionId,
            String merchantId
    ) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id [%s] not found", transactionId)));

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
                .findByMerchantIdAndCreatedAtBetween(merchant.getId(), startDate, endDate);

        return transactions.stream()
                .map(transactionMapper::toTransactionResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelTransaction(
            UUID transactionId,
            String merchantId,
            TransactionStatus transactionStatus
    ) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id [%s] not found", transactionId)));

        if(!transaction.getMerchant().getId().equals(merchantId))
            throw new ForeignMerchantTransactionException(
                    String.format("Transaction with id [%s] belongs to another merchant", transactionId));

        transaction.setStatus(transactionStatus);
    }
}