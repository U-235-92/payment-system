package aq.project.services.wallet;

import aq.project.dto.WalletStatus;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.wallet.WalletRepository;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    private final Fallback fallback;

    @RateLimiter(
            name = "create-wallet",
            fallbackMethod = "createWalletRateLimiterFallback"
    )
    @Transactional
    public UUID createWallet(Wallet wallet) {
        setUpIds(wallet);
        return walletRepository.save(wallet).getId();
    }

    private UUID createWalletRateLimiterFallback(Wallet wallet, Exception exception) {
        String action = "create-wallet";
        String message = String.format(
                "Exception occurred during create wallet for person with id: [%s]. " +
                "Exception message: %s", wallet.getPersonId(), exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    private void setUpIds(Wallet wallet) {
//        Set up Wallet
        UUID walletId = UUID.randomUUID();
//        Set up wallet_id PK
        wallet.setId(walletId);
//        Set up wallet_id for sharding rules
        wallet.getWalletDetails().setWalletId(walletId);
        wallet.getCreditCard().setWalletId(walletId);
//       Set up CreditCard
        UUID creditCardId = UUID.randomUUID();
        wallet.getCreditCard().setId(creditCardId);
//       Set up WalletDetails
        UUID walletDetailsId = UUID.randomUUID();
        wallet.getWalletDetails().setId(walletDetailsId);
    }

    @RateLimiter(
            name = "get-wallet-info",
            fallbackMethod = "getWalletRateLimiterFallback"
    )
    public Wallet getWallet(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No wallet found with id: [%s]", id)));
    }

    private Wallet getWalletRateLimiterFallback(UUID id, Exception exception) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-wallet-info";
        String message = String.format(
                "Exception occurred during getting wallet info for wallet with id: [%s]. " +
                "Exception message: %s", id, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    @Transactional
    public Wallet getWalletWithLock(UUID id) {
        return walletRepository.findByIdWithLock(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No wallet found with id: [%s]", id)));
    }

    @RateLimiter(
            name = "get-wallet-currency-code",
            fallbackMethod = "getWalletCurrencyCodeRateLimiterFallback"
    )
    public String getWalletCurrencyCode(UUID id) {
        return getWallet(id)
                .getWalletDetails()
                .getCurrencyCode();
    }

    private String getWalletCurrencyCodeRateLimiterFallback(UUID id, Exception exception) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-wallet-currency-code";
        String message = String.format(
                "Exception occurred during getting wallet currency code for wallet with id: [%s]. " +
                "Exception message: %s", id, exception.getMessage());

        fallback.handleRateLimiterFallback(action, message, exception);

        return null;
    }

    public boolean isWalletBlocked(Wallet wallet) {
        return wallet.getWalletDetails()
                .getWalletStatus()
                .equals(WalletStatus.BLOCKED);
    }

    public boolean isWalletCreditCardExpired(CreditCard creditCard) {
        return creditCard
                .getCardExpirationDate()
                .isBefore(YearMonth.now());
    }

    public boolean isWalletCreditCardBalanceLessThan(CreditCard creditCard, BigDecimal amount) {
        return creditCard
                .getBalance()
                .compareTo(amount) < 0;
    }
}
