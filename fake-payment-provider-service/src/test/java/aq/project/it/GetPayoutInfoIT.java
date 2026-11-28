package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.MerchantEntities;
import aq.project._utils.entities.PayoutServiceEntities;
import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.entities.Merchant;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.MerchantRepository;
import aq.project.services.PayoutService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetPayoutInfoIT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private PayoutService payoutService;

    @Autowired
    private MerchantRepository merchantRepository;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void cleanOutSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void successGetPayoutInfo() {
//        Arrange
        Merchant merchant = MerchantEntities.getMerchant();

        merchantRepository.save(merchant);

        String merchantId = merchant.getMerchantId();
        String merchantSecret = merchant.getSecretKey();

        Authentication authentication = new UsernamePasswordAuthenticationToken(merchantId, merchantSecret);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);

        CreatePayoutRequestDto createPayoutRequestDto = PayoutServiceEntities.getCreatePayoutRequestDto();
        createPayoutRequestDto.setMerchantId(merchantId);

        PayoutInfoResponseDto payoutInfoResponseDto = payoutService
                .createPayout(createPayoutRequestDto, authentication);

        UUID transactionId = payoutInfoResponseDto.getId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> payoutService.getPayoutInfo(transactionId, authentication));
    }

    @Test
    public void failureOnInfoNotFound() {
//        Arrange
        Merchant merchant = MerchantEntities.getMerchant();

        merchantRepository.save(merchant);

        String merchantId = merchant.getMerchantId();
        String merchantSecret = merchant.getSecretKey();

        Authentication authentication = new UsernamePasswordAuthenticationToken(merchantId, merchantSecret);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);

        UUID transactionId = UUID.randomUUID();

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> payoutService.getPayoutInfo(transactionId, authentication));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        Merchant merchant = MerchantEntities.getMerchant();

        merchantRepository.save(merchant);

        String merchantId = merchant.getMerchantId();
        String merchantSecret = merchant.getSecretKey();

        Authentication authentication = new UsernamePasswordAuthenticationToken(merchantId, merchantSecret);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);

        CreatePayoutRequestDto createPayoutRequestDto = PayoutServiceEntities.getCreatePayoutRequestDto();
        createPayoutRequestDto.setMerchantId(merchantId);

        PayoutInfoResponseDto payoutInfoResponseDto = payoutService.createPayout(createPayoutRequestDto, authentication);

        UUID transactionId = payoutInfoResponseDto.getId();

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> payoutService.getPayoutInfo(transactionId, authentication));
    }
}