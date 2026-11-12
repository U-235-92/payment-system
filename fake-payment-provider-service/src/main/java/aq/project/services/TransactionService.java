package aq.project.services;

import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.Status;
import aq.project.dto.TransactionInfoResponseDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.NotEqualsEntityException;
import aq.project.mappers.TransactionMapper;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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

    private final Fallback fallback;

    @RateLimiter(
            name = "create-transaction-rate-limiter",
            fallbackMethod = "createTransactionFallback"
    )
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

    private TransactionInfoResponseDto createTransactionFallback(
            CreateTransactionRequestDto createTransactionRequestDto,
            Authentication authentication,
            Exception exception
    ) throws Exception {
        if(exception instanceof NotEqualsEntityException
                || exception instanceof EntityNotFoundException)
            throw exception;

        String action = "create-transaction";

        String message = String.format(
                "Exception occurred during creating of transaction for merchant with id: [%s]. " +
                        "Exception message: %s",
                createTransactionRequestDto.getMerchantId(), exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "get-transaction-list-rate-limiter",
            fallbackMethod = "getTransactionListFallback"
    )
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

    private List<TransactionInfoResponseDto> getTransactionListFallback(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Authentication authentication,
            Exception exception
    ) {
        String action = "get-transaction-list";
        String authenticatedMerchantId = authentication.getName();

        String message = String.format(
                "Exception occurred during getting of transaction list for merchant with id: [%s]. " +
                        "Exception message: %s",
                authenticatedMerchantId, exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "get-transaction-info-rate-limiter",
            fallbackMethod = "getTransactionInfoFallback"
    )
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

    private TransactionInfoResponseDto getTransactionInfoFallback(
            Long id,
            Authentication authentication,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-transaction-info";
        String authenticatedMerchantId = authentication.getName();

        String message = String.format(
                "Exception occurred during getting of transaction info for merchant with id: [%s] and transaction id: [%s]. " +
                        "Exception message: %s",
                authenticatedMerchantId, id, exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }
}