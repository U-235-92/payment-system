package aq.project._utils;

import aq.project.dto.CardType;
import aq.project.dto.WalletStatus;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.entities.wallet.WalletDetails;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public final class WalletEntities {

// ==================== WalletDetails ====================

    public static WalletDetails getValidWalletDetails() {
        WalletDetails details = new WalletDetails();
        details.setId(UUID.randomUUID());
        details.setWalletId(UUID.randomUUID());
        details.setCreator("Creator");
        details.setModifier("Modifier");
        details.setCurrencyCode("USD");
        details.setWalletStatus(WalletStatus.ACTIVE);
        details.setArchivedAt(null);
        return details;
    }

    public static WalletDetails getInvalidWalletDetails() {
        WalletDetails details = new WalletDetails();
        details.setId(UUID.randomUUID());
        details.setWalletId(UUID.randomUUID());
        details.setCreator(""); // нарушает @NotBlank
        details.setModifier("Modifier");
        details.setCurrencyCode("USD");
        details.setWalletStatus(WalletStatus.ACTIVE);
        details.setArchivedAt(null);
        return details;
    }

    public static WalletDetails getWalletDetailsWithStatus(WalletStatus status) {
        WalletDetails details = getValidWalletDetails();
        details.setWalletStatus(status);
        return details;
    }

// ==================== CreditCard ====================

    public static CreditCard getValidCreditCard() {
        CreditCard card = new CreditCard();
        card.setId(UUID.randomUUID());
        card.setWalletId(UUID.randomUUID());
        card.setCardNumber("1234 5678 9012 3456");
        card.setCardCvvNumber("123");
        card.setCardExpirationDate(YearMonth.now().plusYears(3));
        card.setCardType(CardType.VISA);
        card.setBalance(BigDecimal.valueOf(1000.00));
        return card;
    }

    public static CreditCard getInvalidCreditCard() {
        CreditCard card = new CreditCard();
        card.setId(UUID.randomUUID());
        card.setWalletId(UUID.randomUUID());
        card.setCardNumber("1234-5678-9012-3456"); // нарушает регулярку
        card.setCardCvvNumber("123");
        card.setCardExpirationDate(YearMonth.now().plusYears(3));
        card.setCardType(CardType.VISA);
        card.setBalance(BigDecimal.valueOf(1000.00));
        return card;
    }

    public static CreditCard getInvalidCreditCardExpired() {
        CreditCard card = new CreditCard();
        card.setId(UUID.randomUUID());
        card.setWalletId(UUID.randomUUID());
        card.setCardNumber("1234 5678 9012 3456");
        card.setCardCvvNumber("123");
        card.setCardExpirationDate(YearMonth.now().minusMonths(1));
        card.setCardType(CardType.VISA);
        card.setBalance(BigDecimal.valueOf(1000.00));
        return card;
    }

    public static CreditCard getInvalidCreditCardCvv() {
        CreditCard card = getValidCreditCard();
        card.setCardCvvNumber("12"); // длина != 3
        return card;
    }

    public static CreditCard getCreditCardWithType(CardType type) {
        CreditCard card = getValidCreditCard();
        card.setCardType(type);
        return card;
    }

// ==================== Wallet ====================

    public static Wallet getValidWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setPersonId(UUID.randomUUID());
        wallet.setWalletDetails(getValidWalletDetails());
        wallet.setCreditCard(getValidCreditCard());
        wallet.setArchivedAt(null);
        return wallet;
    }

    public static Wallet getValidWallet(UUID walletId) {
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setPersonId(UUID.randomUUID());
        wallet.setWalletDetails(getValidWalletDetails());
        wallet.setCreditCard(getValidCreditCard());
        wallet.setArchivedAt(null);
        return wallet;
    }

    public static Wallet getInvalidWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setPersonId(null); // нарушает @NotNull
        wallet.setWalletDetails(getValidWalletDetails());
        wallet.setCreditCard(getValidCreditCard());
        wallet.setArchivedAt(null);
        return wallet;
    }

    public static Wallet getValidWalletBlocked() {
        Wallet wallet = getValidWallet();
        wallet.getWalletDetails().setWalletStatus(WalletStatus.BLOCKED);
        return wallet;
    }

    public static Wallet getValidWalletBlocked(UUID walletId) {
        Wallet wallet = getValidWallet(walletId);
        wallet.getWalletDetails().setWalletStatus(WalletStatus.BLOCKED);
        return wallet;
    }

    public static Wallet getWalletWithPersonId(UUID personId) {
        Wallet wallet = getValidWallet();
        wallet.setPersonId(personId);
        return wallet;
    }

    public static Wallet getWalletWithWalletDetails(WalletDetails details) {
        Wallet wallet = getValidWallet();
        wallet.setWalletDetails(details);
        return wallet;
    }

    public static Wallet getWalletWithCreditCard(CreditCard card) {
        Wallet wallet = getValidWallet();
        wallet.setCreditCard(card);
        return wallet;
    }
}