package aq.project._utils.entities;

import aq.project.dto.*;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.entities.PaymentMethodDefinition;
import aq.project.entities.PaymentMethodRequiredField;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PaymentServiceEntities {

    public static PayoutInfoResponseDto getPayoutInfoResponseDto() {
        PayoutInfoResponseDto payoutInfoResponseDto = new PayoutInfoResponseDto();

        payoutInfoResponseDto.setId(1L);
        payoutInfoResponseDto.setMerchantId(UUID.randomUUID().toString());
        payoutInfoResponseDto.setAmount(BigDecimal.valueOf(85.58));
        payoutInfoResponseDto.setCurrencyCode("RUB");
        payoutInfoResponseDto.setStatus(Status.PENDING);
        payoutInfoResponseDto.setCreatedAt(OffsetDateTime.now());
        payoutInfoResponseDto.setUpdatedAt(OffsetDateTime.now());
        payoutInfoResponseDto.setExternalId(UUID.randomUUID().toString());
        payoutInfoResponseDto.setNotificationUrl("https://example.aq");

        return payoutInfoResponseDto;
    }

    public static CreatePaymentRequestDto getCreatePaymentRequestDto() {
        CreatePaymentRequestDto createPaymentRequestDto = new CreatePaymentRequestDto();

        createPaymentRequestDto.setTransactionId(UUID.randomUUID());
        createPaymentRequestDto.setPaymentMethodId(1L);
        createPaymentRequestDto.setTransactionType(TransactionType.DEPOSIT);
        createPaymentRequestDto.setAmount(BigDecimal.valueOf(85.58));
        createPaymentRequestDto.setCurrencyCode("RUB");

        return createPaymentRequestDto;
    }

    public static Payment getPayment(
            PaymentMethod paymentMethod
    ) {
        Payment payment = new Payment();

        payment.setId(null);
        payment.setExternalId(1L);
        payment.setPaymentId(UUID.randomUUID());
        payment.setTransactionId(UUID.randomUUID());
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionType(TransactionType.DEPOSIT);
        payment.setAmount(BigDecimal.valueOf(85.58));
        payment.setCurrencyCode("RUB");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setUpdatedAt(OffsetDateTime.now());
        payment.setProcessed(false);

        return payment;
    }

    public static PaymentMethod getPaymentMethod(
            PaymentMethodDefinition paymentMethodDefinition,
            PaymentMethodRequiredField paymentMethodRequiredField
    ) {
        PaymentMethod paymentMethod = new PaymentMethod();

        paymentMethod.setId(null);
        paymentMethod.setDefinition(paymentMethodDefinition);
        paymentMethod.setRequiredFields(paymentMethodRequiredField);
        paymentMethod.setCreatedAt(OffsetDateTime.now());
        paymentMethod.setUpdatedAt(OffsetDateTime.now());
        paymentMethod.setMethod(aq.project.dto.PaymentMethod.TRANSFER);
        paymentMethod.setLogo("AQ");

        return paymentMethod;
    }

    public static PaymentMethodDefinition getPaymentMethodDefinition() {
        PaymentMethodDefinition paymentMethodDefinition = new PaymentMethodDefinition();

        paymentMethodDefinition.setId(null);

        return paymentMethodDefinition;
    }

    public static PaymentMethodRequiredField getPaymentMethodRequiredField() {
        PaymentMethodRequiredField paymentMethodRequiredField = new PaymentMethodRequiredField();

        paymentMethodRequiredField.setId(null);
        paymentMethodRequiredField.setCreatedAt(OffsetDateTime.now());
        paymentMethodRequiredField.setUpdatedAt(OffsetDateTime.now());
        paymentMethodRequiredField.setCountryAlpha3Code(genString(3, false));
        paymentMethodRequiredField.setCurrencyCode(genString(3, false));
        paymentMethodRequiredField.setName(genString(10, false));
        paymentMethodRequiredField.setDataType("DataType");
        paymentMethodRequiredField.setValidationType("ValidationType");
        paymentMethodRequiredField.setValidationRule("ValidationRule");
        paymentMethodRequiredField.setDefaultValue("DefaultValue");
        paymentMethodRequiredField.setValuesOptions("ValuesOptions");
        paymentMethodRequiredField.setDescription("Description");
        paymentMethodRequiredField.setPlaceholder("Placeholder");
        paymentMethodRequiredField.setRepresentationName("RepresentationName");
        paymentMethodRequiredField.setLanguage(genString(2, true));
        paymentMethodRequiredField.setImageUrl("https://image.aq/image");

        return paymentMethodRequiredField;
    }

    private static String genString(int length, boolean isLowerCase) {
        String[] alphabet = Stream.iterate(97, num -> num + 1)
                    .limit(26)
                    .map(Character::toString)
                    .map(symbol -> isLowerCase ? symbol.toLowerCase() : symbol.toUpperCase())
                    .toArray(String[]::new);

        return genString(alphabet, length);
    }

    private static String genString(String[] alphabet, int length) {
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < length; i++) {
            result.append(alphabet[(int) (Math.random() * alphabet.length)]);
        }

        return result.toString();
    }
}