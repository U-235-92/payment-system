package aq.project.services;

import aq.project.dto.PaymentProviderServiceTransactionInfoResponseDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(readOnly = true)
    public PaymentProviderServiceTransactionInfoResponseDto getTransactionInfo(
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
    public List<PaymentProviderServiceTransactionInfoResponseDto> getTransactionList(
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