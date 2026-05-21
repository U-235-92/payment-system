package aq.project.services;

import aq.project.entities.InstantEmbeddedData;
import aq.project.entities.Wallet;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
//       Set up Wallet
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

    public Wallet getWalletInfo(String walletId) throws NoSuchWalletException {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new NoSuchWalletException(walletId));
    }
}
