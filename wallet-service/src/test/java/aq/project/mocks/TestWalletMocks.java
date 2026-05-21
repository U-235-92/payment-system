package aq.project.mocks;

import aq.project.dto.CardType;
import aq.project.dto.WalletStatus;
import aq.project.entities.CreditCard;
import aq.project.entities.InstantEmbeddedData;
import aq.project.entities.Wallet;
import aq.project.entities.WalletDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TestWalletMocks {

    public static Wallet getValidWalletMock() {
        InstantEmbeddedData instantEmbeddedData = new InstantEmbeddedData();

        String walletId = UUID.randomUUID().toString();

        WalletDetails walletDetails = new WalletDetails();
        walletDetails.setId(UUID.randomUUID().toString());
        walletDetails.setWalletId(walletId);
        walletDetails.setCreator("Creator");
        walletDetails.setModifier("Modifier");
        walletDetails.setArchivedAt(instantEmbeddedData.getCreatedAt());
        walletDetails.setCurrencyCode("RUB");
        walletDetails.setWalletStatus(WalletStatus.ACTIVE);
        walletDetails.setInstantEmbeddedData(instantEmbeddedData);

        CreditCard creditCard = new CreditCard();
        creditCard.setId(UUID.randomUUID().toString());
        creditCard.setWalletId(walletId);
        creditCard.setCardNumber("4000 0012 3456 7899");
        creditCard.setCardCvvNumber("588");
        creditCard.setCardType(CardType.VISA);
        creditCard.setCardExpirationDate(YearMonth.parse("08/55", DateTimeFormatter.ofPattern("MM/yy")));
        creditCard.setBalance(new BigDecimal("8585.85"));
        creditCard.setInstantEmbeddedData(instantEmbeddedData);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCreditCard(creditCard);
        wallet.setPersonId(UUID.randomUUID().toString());
        wallet.setWalletDetails(walletDetails);
        wallet.setInstantEmbeddedData(instantEmbeddedData);
        wallet.setArchivedAt(instantEmbeddedData.getCreatedAt());

        return wallet;
    }

    public static Wallet getValidWalletMock(String walletId) {
        InstantEmbeddedData instantEmbeddedData = new InstantEmbeddedData();

        WalletDetails walletDetails = new WalletDetails();
        walletDetails.setId(UUID.randomUUID().toString());
        walletDetails.setWalletId(walletId);
        walletDetails.setCreator("Creator");
        walletDetails.setModifier("Modifier");
        walletDetails.setArchivedAt(instantEmbeddedData.getCreatedAt());
        walletDetails.setCurrencyCode("RUB");
        walletDetails.setWalletStatus(WalletStatus.ACTIVE);
        walletDetails.setInstantEmbeddedData(instantEmbeddedData);

        CreditCard creditCard = new CreditCard();
        creditCard.setId(UUID.randomUUID().toString());
        creditCard.setWalletId(walletId);
        creditCard.setCardNumber("4000 0012 3456 7899");
        creditCard.setCardCvvNumber("588");
        creditCard.setCardType(CardType.VISA);
        creditCard.setCardExpirationDate(YearMonth.parse("08/55", DateTimeFormatter.ofPattern("MM/yy")));
        creditCard.setBalance(new BigDecimal("8585.85"));
        creditCard.setInstantEmbeddedData(instantEmbeddedData);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCreditCard(creditCard);
        wallet.setPersonId(UUID.randomUUID().toString());
        wallet.setWalletDetails(walletDetails);
        wallet.setInstantEmbeddedData(instantEmbeddedData);
        wallet.setArchivedAt(instantEmbeddedData.getCreatedAt());

        return wallet;
    }

    public static Wallet getInvalidWalletMock() {
        InstantEmbeddedData instantEmbeddedData = new InstantEmbeddedData();

        String walletId = UUID.randomUUID().toString();

        WalletDetails walletDetails = new WalletDetails();
        walletDetails.setId(UUID.randomUUID().toString());
        walletDetails.setWalletId(walletId);
        walletDetails.setCreator(null);
        walletDetails.setModifier(null);
        walletDetails.setArchivedAt(-1L);
        walletDetails.setCurrencyCode("abc");
        walletDetails.setWalletStatus(WalletStatus.ACTIVE);
        walletDetails.setInstantEmbeddedData(instantEmbeddedData);

        CreditCard creditCard = new CreditCard();
        creditCard.setId(UUID.randomUUID().toString());
        creditCard.setWalletId(walletId);
        creditCard.setCardNumber("4000 0012 3456 7899");
        creditCard.setCardCvvNumber("588");
        creditCard.setCardType(CardType.VISA);
        creditCard.setCardExpirationDate(YearMonth.parse("08/55", DateTimeFormatter.ofPattern("MM/yy")));
        creditCard.setBalance(new BigDecimal("8585.8585"));
        creditCard.setInstantEmbeddedData(instantEmbeddedData);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCreditCard(creditCard);
        wallet.setPersonId(UUID.randomUUID().toString());
        wallet.setWalletDetails(walletDetails);
        wallet.setInstantEmbeddedData(instantEmbeddedData);
        wallet.setArchivedAt(instantEmbeddedData.getCreatedAt());

        return wallet;
    }

    public static Wallet getInvalidWalletMockWithWrongUuidData() {
        InstantEmbeddedData instantEmbeddedData = new InstantEmbeddedData();

        String walletId = UUID.randomUUID().toString();

        WalletDetails walletDetails = new WalletDetails();
        walletDetails.setId(UUID.randomUUID().toString());
        walletDetails.setWalletId(walletId);
        walletDetails.setCreator("Creator");
        walletDetails.setModifier("Modifier");
        walletDetails.setArchivedAt(instantEmbeddedData.getCreatedAt());
        walletDetails.setCurrencyCode("RUB");
        walletDetails.setWalletStatus(WalletStatus.ACTIVE);
        walletDetails.setInstantEmbeddedData(instantEmbeddedData);

        CreditCard creditCard = new CreditCard();
        creditCard.setId(UUID.randomUUID().toString());
        creditCard.setWalletId(walletId);
        creditCard.setCardNumber("4000 0012 3456 7899");
        creditCard.setCardCvvNumber("588");
        creditCard.setCardType(CardType.VISA);
        creditCard.setCardExpirationDate(YearMonth.parse("08/55", DateTimeFormatter.ofPattern("MM/yy")));
        creditCard.setBalance(new BigDecimal("8585.85"));
        creditCard.setInstantEmbeddedData(instantEmbeddedData);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCreditCard(creditCard);
        wallet.setPersonId(UUID.fromString("abc").toString());
        wallet.setWalletDetails(walletDetails);
        wallet.setInstantEmbeddedData(instantEmbeddedData);
        wallet.setArchivedAt(instantEmbeddedData.getCreatedAt());

        return wallet;
    }

    public static Wallet getBlockedWalletMock() {
        InstantEmbeddedData instantEmbeddedData = new InstantEmbeddedData();

        String walletId = UUID.randomUUID().toString();

        WalletDetails walletDetails = new WalletDetails();
        walletDetails.setId(UUID.randomUUID().toString());
        walletDetails.setWalletId(walletId);
        walletDetails.setCreator("Creator");
        walletDetails.setModifier("Modifier");
        walletDetails.setArchivedAt(instantEmbeddedData.getCreatedAt());
        walletDetails.setCurrencyCode("RUB");
        walletDetails.setWalletStatus(WalletStatus.BLOCKED);
        walletDetails.setInstantEmbeddedData(instantEmbeddedData);

        CreditCard creditCard = new CreditCard();
        creditCard.setId(UUID.randomUUID().toString());
        creditCard.setWalletId(walletId);
        creditCard.setCardNumber("4000 0012 3456 7899");
        creditCard.setCardCvvNumber("588");
        creditCard.setCardType(CardType.VISA);
        creditCard.setCardExpirationDate(YearMonth.parse("08/55", DateTimeFormatter.ofPattern("MM/yy")));
        creditCard.setBalance(new BigDecimal("8585.85"));
        creditCard.setInstantEmbeddedData(instantEmbeddedData);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCreditCard(creditCard);
        wallet.setPersonId(UUID.randomUUID().toString());
        wallet.setWalletDetails(walletDetails);
        wallet.setInstantEmbeddedData(instantEmbeddedData);
        wallet.setArchivedAt(instantEmbeddedData.getCreatedAt());

        return wallet;
    }

    public static Wallet getExpiredCardValidWalletMock() {
        InstantEmbeddedData instantEmbeddedData = new InstantEmbeddedData();

        String walletId = UUID.randomUUID().toString();

        WalletDetails walletDetails = new WalletDetails();
        walletDetails.setId(UUID.randomUUID().toString());
        walletDetails.setWalletId(walletId);
        walletDetails.setCreator("Creator");
        walletDetails.setModifier("Modifier");
        walletDetails.setArchivedAt(instantEmbeddedData.getCreatedAt());
        walletDetails.setCurrencyCode("RUB");
        walletDetails.setWalletStatus(WalletStatus.ACTIVE);
        walletDetails.setInstantEmbeddedData(instantEmbeddedData);

        CreditCard creditCard = new CreditCard();
        creditCard.setId(UUID.randomUUID().toString());
        creditCard.setWalletId(walletId);
        creditCard.setCardNumber("4000 0012 3456 7899");
        creditCard.setCardCvvNumber("588");
        creditCard.setCardType(CardType.VISA);
        creditCard.setCardExpirationDate(YearMonth.parse("08/25", DateTimeFormatter.ofPattern("MM/yy")));
        creditCard.setBalance(new BigDecimal("8585.85"));
        creditCard.setInstantEmbeddedData(instantEmbeddedData);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setCreditCard(creditCard);
        wallet.setPersonId(UUID.randomUUID().toString());
        wallet.setWalletDetails(walletDetails);
        wallet.setInstantEmbeddedData(instantEmbeddedData);
        wallet.setArchivedAt(instantEmbeddedData.getCreatedAt());

        return wallet;
    }
}
