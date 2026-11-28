package aq.project.services;

import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.dto.Status;
import aq.project.entities.Merchant;
import aq.project.entities.Payout;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.mappers.PayoutMapper;
import aq.project.repositories.PayoutRepository;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {
    
    private final PayoutMapper payoutMapper = PayoutMapper.INSTANCE;

    private final PayoutRepository payoutRepository;

    private final MerchantService merchantService;

    private final Fallback fallback;

    @RateLimiter(
            name = "create-payout-rate-limiter",
            fallbackMethod = "createPayoutRateLimiterFallback"
    )
    @Transactional
    public PayoutInfoResponseDto createPayout(
            CreatePayoutRequestDto createPayoutRequestDto,
            Authentication authentication
    ) {
        String requestMerchantId = createPayoutRequestDto.getMerchantId();
        String authenticatedMerchantId = authentication.getName();
        String externalId = createPayoutRequestDto.getExternalId();

        merchantService.checkEqualMerchantIds(requestMerchantId, authenticatedMerchantId);

        Merchant merchant = merchantService.getMerchant(requestMerchantId);

        Payout payout;

//        Check idempotent create payout call.
//        In case if payout already exists then return its info without creation of duplicate.
        if(externalId == null)
            payout = createPayout(createPayoutRequestDto, merchant);
        else
            payout = payoutRepository.findByExternalId(externalId, authenticatedMerchantId)
                    .orElseGet(() -> createPayout(createPayoutRequestDto, merchant));

        log.debug("Success handle of request to create payout for merchant with id: [{}] " +
                        "and transaction id [{}] with response body: {}",
                requestMerchantId, externalId, payoutMapper.toPayoutInfoResponseDto(payout));

        return payoutMapper.toPayoutInfoResponseDto(payout);
    }

    private PayoutInfoResponseDto createPayoutRateLimiterFallback(
            CreatePayoutRequestDto createPayoutRequestDto,
            Authentication authentication,
            RequestNotPermitted exception
    ) {
        String action = "create-payout";

        String message = String.format(
                "Exception occurred during creating of payout for merchant with id: [%s]. " +
                        "Exception message: %s",
                createPayoutRequestDto.getMerchantId(), exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }

    private Payout createPayout(
            CreatePayoutRequestDto createPayoutRequestDto,
            Merchant merchant
    ) {
        Payout newPayout = payoutMapper.toPayout(createPayoutRequestDto);

        newPayout.setMerchant(merchant);
        newPayout.setStatus(Status.PENDING);

//        Continue to async handle saved payout.
//        See PayoutScheduler.updatePayoutStatus(...).
        return payoutRepository.save(newPayout);
    }

    @RateLimiter(
            name = "get-payout-list-rate-limiter",
            fallbackMethod = "getPayoutListRateLimiterFallback"
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

    private List<PayoutInfoResponseDto> getPayoutListRateLimiterFallback(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Authentication authentication,
            RequestNotPermitted exception
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
            fallbackMethod = "getPayoutInfoRateLimiterFallback"
    )
    public PayoutInfoResponseDto getPayoutInfo(
            UUID id,
            Authentication authentication
    ) {
        String authenticatedMerchantId = authentication.getName();

        return payoutRepository.findByIdAndMerchantId(id, authenticatedMerchantId)
                .map(payoutMapper::toPayoutInfoResponseDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Payout with id: [%s] not found for merchant with merchant id: [%s]",
                                id, authenticatedMerchantId)));
    }

    private PayoutInfoResponseDto getPayoutInfoRateLimiterFallback(
            Long id,
            Authentication authentication,
            RequestNotPermitted exception
    ) throws Exception {
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