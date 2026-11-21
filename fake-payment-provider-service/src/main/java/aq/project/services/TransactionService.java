package aq.project.services;

import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.Status;
import aq.project.dto.TransactionInfoResponseDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.mappers.TransactionMapper;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final Fallback fallback;

    @RateLimiter(
            name = "create-transaction-rate-limiter",
            fallbackMethod = "createTransactionRateLimiterFallback"
    )
    @Transactional
    public TransactionInfoResponseDto createTransaction(
            CreateTransactionRequestDto createTransactionRequestDto,
            Authentication authentication
    ) {
        String requestMerchantId = createTransactionRequestDto.getMerchantId();
        String authenticatedMerchantId = authentication.getName();
        String externalId = createTransactionRequestDto.getExternalId();

        merchantService.checkEqualMerchantIds(requestMerchantId, authenticatedMerchantId);

        Merchant merchant = merchantService.getMerchant(requestMerchantId);

        Transaction transaction;

//        Check idempotent create transaction call.
//        In case if transaction already exists then return its info without creation of duplicate.
        if(externalId == null)
            transaction = createTransaction(createTransactionRequestDto, merchant);
        else
            transaction = transactionRepository.findByExternalId(externalId, authenticatedMerchantId)
                    .orElse(createTransaction(createTransactionRequestDto, merchant));

        return transactionMapper.toTransactionInfoResponseDto(transaction);
    }

    private TransactionInfoResponseDto createTransactionRateLimiterFallback(
            CreateTransactionRequestDto createTransactionRequestDto,
            Authentication authentication,
            RequestNotPermitted exception
    ) throws Exception {
        String action = "create-transaction";

        String message = String.format(
                "Exception occurred during creating of transaction for merchant with id: [%s]. " +
                        "Exception message: %s",
                createTransactionRequestDto.getMerchantId(), exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }

    private Transaction createTransaction(
            CreateTransactionRequestDto createTransactionRequestDto,
            Merchant merchant
    ) {
        Transaction newTransaction = transactionMapper.toTransaction(createTransactionRequestDto);

        newTransaction.setMerchant(merchant);
        newTransaction.setStatus(Status.PENDING);

//        Continue to async handle saved payout.
//        See TransactionScheduler.updateTransactionStatus for details.
        return transactionRepository.save(newTransaction);
    }

    @RateLimiter(
            name = "get-transaction-list-rate-limiter",
            fallbackMethod = "getTransactionListRateLimiterFallback"
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

    private List<TransactionInfoResponseDto> getTransactionListRateLimiterFallback(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Authentication authentication,
            RequestNotPermitted exception
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
            fallbackMethod = "getTransactionInfoRateLimiterFallback"
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

    private TransactionInfoResponseDto getTransactionInfoRateLimiterFallback(
            Long id,
            Authentication authentication,
            RequestNotPermitted exception
    ) throws Exception {
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