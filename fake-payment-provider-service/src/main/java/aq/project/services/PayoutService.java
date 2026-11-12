package aq.project.services;

import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.dto.Status;
import aq.project.entities.Merchant;
import aq.project.entities.Payout;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.mappers.PayoutMapper;
import aq.project.repositories.PayoutRepository;
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
}
