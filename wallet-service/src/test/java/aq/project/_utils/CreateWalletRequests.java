package aq.project._utils;

import aq.project.dto.CardType;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletStatus;

import java.time.YearMonth;
import java.util.UUID;

public final class CreateWalletRequests {

    public static CreateWalletRequestDto getValidCreateWalletRequest() {
        CreateWalletRequestDto dto = new CreateWalletRequestDto();
        dto.setPersonId(UUID.randomUUID());
        dto.setWalletStatus(WalletStatus.ACTIVE);
        dto.setCreator("Creator");
        dto.setModifier("Modifier");
        dto.setCurrencyCode("USD");
        dto.setBalance("100.00");
        dto.setCardNumber("1234 5678 9012 3456");
        dto.setCardCvvNumber("123");
        // дата в формате MM/YY, например, через 3 года от текущего месяца
        YearMonth future = YearMonth.now().plusYears(3);
        dto.setCardExpirationDate(future.format(java.time.format.DateTimeFormatter.ofPattern("MM/yy")));
        dto.setCardType(CardType.VISA);
        return dto;
    }

    public static CreateWalletRequestDto getInvalidCreateWalletRequest() {
        CreateWalletRequestDto dto = new CreateWalletRequestDto();
        // 1. Нарушаем @NotNull
        dto.setPersonId(null);
        dto.setWalletStatus(null);
        // 2. Нарушаем @Size(max = 255) – можно оставить пустую строку, но она проходит @NotNull, поэтому оставляем null
        //    или сделаем длинную строку >255, но проще оставить null, чтобы нарушить @NotNull
        dto.setCreator(null);
        dto.setModifier(null);
        // 3. Нарушаем @Pattern (длина и содержимое)
        dto.setCurrencyCode("US"); // должно быть 3 буквы
        // 4. Нарушаем @Pattern для balance (должен быть формат "XXX.XX")
        dto.setBalance("not a number");
        // 5. Нарушаем @Pattern для cardNumber (должен быть "XXXX XXXX XXXX XXXX")
        dto.setCardNumber("1234-5678-9012-3456");
        // 6. Нарушаем @Pattern для cvv (ровно 3 цифры)
        dto.setCardCvvNumber("12");
        // 7. Нарушаем @Pattern для expiration (MM/YY)
        dto.setCardExpirationDate("13/25"); // месяц 13 недопустим
        // 8. Нарушаем @NotNull для cardType
        dto.setCardType(null);
        return dto;
    }

    // Дополнительные методы для конкретных нарушений (опционально)

    public static CreateWalletRequestDto getCreateWalletRequestWithNullCreator() {
        CreateWalletRequestDto dto = getValidCreateWalletRequest();
        dto.setCreator(null);
        return dto;
    }

    public static CreateWalletRequestDto getCreateWalletRequestWithInvalidBalance() {
        CreateWalletRequestDto dto = getValidCreateWalletRequest();
        dto.setBalance("-10.00"); // отрицательная сумма, но @Pattern пропускает, так что лучше "abc"
        // Для надёжности используем нечисловое значение
        dto.setBalance("abc");
        return dto;
    }

    public static CreateWalletRequestDto getCreateWalletRequestWithInvalidCardNumber() {
        CreateWalletRequestDto dto = getValidCreateWalletRequest();
        dto.setCardNumber("1234 5678 9012 345"); // не хватает цифр
        return dto;
    }

    public static CreateWalletRequestDto getCreateWalletRequestWithInvalidExpirationDate() {
        CreateWalletRequestDto dto = getValidCreateWalletRequest();
        dto.setCardExpirationDate("12/2"); // неправильный формат
        return dto;
    }
}