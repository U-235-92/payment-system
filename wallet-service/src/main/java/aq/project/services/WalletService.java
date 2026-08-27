package aq.project.services;

import aq.project.dto.WalletStatus;
import aq.project.entities.CreditCard;
import aq.project.entities.InstantEmbeddedData;
import aq.project.entities.Wallet;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.WalletRepository;
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
    public String createWallet(Wallet wallet) {
        setUpIds(wallet);
        setUpWalletTemporalData(wallet);
        return walletRepository.save(wallet).getId();
    }

    private void setUpIds(Wallet wallet) {
//        Set up Wallet
        String walletId = UUID.randomUUID().toString();
//        Set up wallet_id PK
        wallet.setId(walletId);
//        Set up wallet_id for sharding rules
        wallet.getWalletDetails().setWalletId(walletId);
        wallet.getCreditCard().setWalletId(walletId);
//       Set up CreditCard
        String creditCardId = UUID.randomUUID().toString();
        wallet.getCreditCard().setId(creditCardId);
//       Set up WalletDetails
        String walletDetailsId = UUID.randomUUID().toString();
        wallet.getWalletDetails().setId(walletDetailsId);
    }

    private void setUpWalletTemporalData(Wallet wallet) {
        InstantEmbeddedData instantEmbeddedData = new InstantEmbeddedData();
//        Set up wallet's temporal data
        wallet.setInstantEmbeddedData(instantEmbeddedData);
        wallet.setArchivedAt(instantEmbeddedData.getCreatedAt());
//        Set up wallet details' temporal data
        wallet.getWalletDetails().setInstantEmbeddedData(instantEmbeddedData);
        wallet.getWalletDetails().setArchivedAt(instantEmbeddedData.getCreatedAt());
//        Set up wallet credit card's temporal data
        wallet.getCreditCard().setInstantEmbeddedData(instantEmbeddedData);
    }

    public Wallet getWallet(String id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No wallet found with id: [%s]", id)));
    }

    public Wallet getWalletWithLock(String id) {
        return walletRepository.findByIdWithLock(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No wallet found with id: [%s]", id)));
    }

    public String getWalletCurrencyCode(String id) {
        return getWallet(id)
                .getWalletDetails()
                .getCurrencyCode();
    }

    protected boolean isWalletBlocked(Wallet wallet) {
        return wallet.getWalletDetails()
                .getWalletStatus()
                .equals(WalletStatus.BLOCKED);
    }

    protected boolean isWalletCreditCardExpired(CreditCard creditCard) {
        return creditCard
                .getCardExpirationDate()
                .isBefore(YearMonth.now());
    }

    protected boolean isWalletCreditCardBalanceLessThan(CreditCard creditCard, BigDecimal amount) {
        return creditCard
                .getBalance()
                .compareTo(amount) < 0;
    }
}
