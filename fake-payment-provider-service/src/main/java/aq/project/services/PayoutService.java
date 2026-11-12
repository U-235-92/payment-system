package aq.project.services;

import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.dto.Status;
import aq.project.entities.Merchant;
import aq.project.entities.Payout;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.NotEqualsEntityException;
import aq.project.mappers.PayoutMapper;
import aq.project.repositories.PayoutRepository;
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
public class PayoutService {
    
    private final PayoutMapper payoutMapper = PayoutMapper.INSTANCE;

    private final PayoutRepository payoutRepository;

    private final MerchantService merchantService;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Fallback fallback;

    @RateLimiter(
            name = "create-payout-rate-limiter",
            fallbackMethod = "createPayoutFallback"
    )
    @Transactional
    public PayoutInfoResponseDto createPayout(
            CreatePayoutRequestDto createPayoutRequestDto,
            Authentication authentication
    ) {
        String requestMerchantId = createPayoutRequestDto.getMerchantId();
        String authenticatedMerchantId = authentication.getName();

        merchantService.checkEqualMerchantIds(requestMerchantId, authenticatedMerchantId);

        Merchant merchant = merchantService.getMerchant(requestMerchantId);

        Payout newPayout = payoutMapper.toPayout(createPayoutRequestDto);
        
        newPayout.setMerchant(merchant);
        newPayout.setStatus(Status.PENDING);
        
        Payout savedPayout = payoutRepository.save(newPayout);

//        Continue to async handle saved payout (see WebhookService.completePayoutAsync)
        applicationEventPublisher.publishEvent(savedPayout);
        
        return payoutMapper.toPayoutInfoResponseDto(savedPayout);
    }

    private PayoutInfoResponseDto createPayoutFallback(
            CreatePayoutRequestDto createPayoutRequestDto,
            Authentication authentication,
            Exception exception
    ) throws Exception {
        if(exception instanceof NotEqualsEntityException
                || exception instanceof EntityNotFoundException)
            throw exception;

        String action = "create-payout";

        String message = String.format(
                "Exception occurred during creating of payout for merchant with id: [%s]. " +
                        "Exception message: %s",
                createPayoutRequestDto.getMerchantId(), exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "get-payout-list-rate-limiter",
            fallbackMethod = "getPayoutListFallback"
    )
    public List<PayoutInfoResponseDto> getPayoutList(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Authentication authentication
    ) {
        String authenticatedMerchantId = authentication.getName();

        if(startDate != null && endDate != null) {
            if(startDate.isBefore(endDate)) {
                return payoutRepository
                        .findByMerchantId(authenticatedMerchantId, startDate, endDate)
                        .stream()
                        .map(payoutMapper::toPayoutInfoResponseDto)
                        .toList();
            } else {
                throw new IllegalStateException("Start date must be before end date");
            }
        }

        return payoutRepository
                .findByMerchantId(authenticatedMerchantId)
                .stream()
                .map(payoutMapper::toPayoutInfoResponseDto)
                .toList();
    }

    private List<PayoutInfoResponseDto> getPayoutListFallback(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Authentication authentication,
            Exception exception
    ) {
        String action = "get-payout-list";
        String authenticatedMerchantId = authentication.getName();

        String message = String.format(
                "Exception occurred during getting of payout list for merchant with id: [%s]. " +
                        "Exception message: %s",
                authenticatedMerchantId, exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "get-payout-info-rate-limiter",
            fallbackMethod = "getPayoutInfoFallback"
    )
    public PayoutInfoResponseDto getPayoutInfo(
            Long id,
            Authentication authentication
    ) {
        String authenticatedMerchantId = authentication.getName();

        return payoutRepository.findByIdAndMerchantId(id, authenticatedMerchantId)
                .map(payoutMapper::toPayoutInfoResponseDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Payout with id: [%s] not found for merchant with merchant id: [%s]",
                                id, authenticatedMerchantId)));
    }

    private PayoutInfoResponseDto getPayoutInfoFallback(
            Long id,
            Authentication authentication,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-payout-info";
        String authenticatedMerchantId = authentication.getName();

        String message = String.format(
                "Exception occurred during getting of payout info for merchant with id: [%s] and payout id: [%s]. " +
                        "Exception message: %s",
                authenticatedMerchantId, id, exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }
}
