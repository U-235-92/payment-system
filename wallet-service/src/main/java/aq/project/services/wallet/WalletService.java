package aq.project.services.wallet;

import aq.project.dto.WalletStatus;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.wallet.WalletRepository;
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

    @Transactional
    public UUID createWallet(Wallet wallet) {
        setUpIds(wallet);
        return walletRepository.save(wallet).getId();
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

    public Wallet getWallet(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No wallet found with id: [%s]", id)));
    }

    public Wallet getWalletWithLock(UUID id) {
        return walletRepository.findByIdWithLock(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No wallet found with id: [%s]", id)));
    }

    public String getWalletCurrencyCode(UUID id) {
        return getWallet(id)
                .getWalletDetails()
                .getCurrencyCode();
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
