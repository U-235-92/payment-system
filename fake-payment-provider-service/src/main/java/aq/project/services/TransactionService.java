package aq.project.services;

import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.Status;
import aq.project.dto.TransactionInfoResponseDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.mappers.TransactionMapper;
import aq.project.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final TransactionRepository transactionRepository;

    private final MerchantService merchantService;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public TransactionInfoResponseDto createTransaction(
            CreateTransactionRequestDto createTransactionRequestDto,
            Authentication authentication
    ) {
        String requestMerchantId = createTransactionRequestDto.getMerchantId();
        String authenticatedMerchantId = authentication.getName();

        merchantService.checkEqualMerchantIds(requestMerchantId, authenticatedMerchantId);

        Merchant merchant = merchantService.getMerchant(requestMerchantId);

        Transaction newTransaction = transactionMapper.toTransaction(createTransactionRequestDto);

        newTransaction.setMerchant(merchant);
        newTransaction.setStatus(Status.PENDING);

        Transaction savedTransaction = transactionRepository.save(newTransaction);

//        Continue to async handle saved transaction (see WebhookService.completeTransactionAsync)
        applicationEventPublisher.publishEvent(savedTransaction);

        return transactionMapper.toTransactionInfoResponseDto(savedTransaction);
    }

    public List<TransactionInfoResponseDto> getTransactionList(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Authentication authentication
    ) {
        String authenticatedMerchantId = authentication.getName();

        if(startDate != null && endDate != null) {
            if(startDate.isBefore(endDate)) {
                return transactionRepository
                        .findByMerchantId(authenticatedMerchantId, startDate, endDate)
                        .stream()
                        .map(transactionMapper::toTransactionInfoResponseDto)
                        .toList();
            } else {
                throw new IllegalStateException("Start date must be before end date");
            }
        }

        return transactionRepository
                .findByMerchantId(authenticatedMerchantId)
                .stream()
                .map(transactionMapper::toTransactionInfoResponseDto)
                .toList();
    }

    public TransactionInfoResponseDto getTransactionInfo(
            Long id,
            Authentication authentication
    ) {
        String authenticatedMerchantId = authentication.getName();

        return transactionRepository.findByIdAndMerchantId(id, authenticatedMerchantId)
                .map(transactionMapper::toTransactionInfoResponseDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id: [%s] not found for merchant with merchant id: [%s]",
                                id, authenticatedMerchantId)));
    }
}