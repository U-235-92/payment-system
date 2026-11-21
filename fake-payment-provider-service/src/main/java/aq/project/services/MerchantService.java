package aq.project.services;

import aq.project.entities.Merchant;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.NotEqualsEntityException;
import aq.project.repositories.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    void checkEqualMerchantIds(String requestMerchantId, String authenticatedMerchantId) {
        if(!requestMerchantId.equals(authenticatedMerchantId))
            onNotEqualsMerchantId(requestMerchantId, authenticatedMerchantId);
    }

    private void onNotEqualsMerchantId(
            String requestMerchantId,
            String authenticatedMerchantId
    ) {
        throw new NotEqualsEntityException(
                String.format("Received request contains merchant id: [%s] " +
                                "which is not equal to authenticated merchant with id: [%s]",
                        requestMerchantId, authenticatedMerchantId));
    }

    Merchant getMerchant(String merchantId) {
        return merchantRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Merchant with id: [%s] not found", merchantId)));
    }
}
