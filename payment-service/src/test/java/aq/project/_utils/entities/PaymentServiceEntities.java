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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PaymentServiceEntities {

    public static PaymentServiceCreatePaymentRequestDto getValidPaymentServiceCreatePaymentRequestDto() {
        PaymentServiceCreatePaymentRequestDto dto = new PaymentServiceCreatePaymentRequestDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setPaymentMethodId(1L);
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setCurrencyCode("USD");
        dto.setTraceId(UUID.randomUUID().toString());
        return dto;
    }

    public static PaymentServiceCreatePaymentRequestDto getInvalidPaymentServiceCreatePaymentRequestDto() {
        PaymentServiceCreatePaymentRequestDto dto = new PaymentServiceCreatePaymentRequestDto();
        dto.setTransactionId(null);
        dto.setPaymentMethodId(1L);
        dto.setAmount(BigDecimal.valueOf(-100.00));
        dto.setCurrencyCode("TEST");
        dto.setTraceId(UUID.randomUUID().toString());
        return dto;
    }

    public static Payment getValidPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentId(UUID.randomUUID());
        payment.setTransactionId(UUID.randomUUID());
        payment.setPaymentMethod(getValidPaymentMethod());
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setCurrencyCode("USD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setUpdatedAt(OffsetDateTime.now());
        return payment;
    }

    public static PaymentMethod getValidPaymentMethod() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setRequiredFields(getValidPaymentMethodRequiredField());
        paymentMethod.setDefinition(getValidPaymentMethodDefinition());
        paymentMethod.setCreatedAt(OffsetDateTime.now());
        paymentMethod.setUpdatedAt(OffsetDateTime.now());
        paymentMethod.setType(PaymentType.BANK_TRANSFER);
        paymentMethod.setLogo("AQ");
        return paymentMethod;
    }

    public static PaymentMethodRequiredField getValidPaymentMethodRequiredField() {
        PaymentMethodRequiredField paymentMethodRequiredField = new PaymentMethodRequiredField();
        paymentMethodRequiredField.setId(1L);
        paymentMethodRequiredField.setCreatedAt(OffsetDateTime.now());
        paymentMethodRequiredField.setUpdatedAt(OffsetDateTime.now());
        paymentMethodRequiredField.setCountryAlpha3Code("ATA");
        paymentMethodRequiredField.setCurrencyCode("USD");
        paymentMethodRequiredField.setName("Antarctica Inc.");
        paymentMethodRequiredField.setDataType("DataType");
        paymentMethodRequiredField.setValidationType("ValidationType");
        paymentMethodRequiredField.setValidationRule("ValidationRule");
        paymentMethodRequiredField.setDefaultValue("DefaultValue");
        paymentMethodRequiredField.setValuesOptions("ValuesOptions");
        paymentMethodRequiredField.setDescription("Description");
        paymentMethodRequiredField.setPlaceholder("Placeholder");
        paymentMethodRequiredField.setRepresentationName("RepresentationName");
        paymentMethodRequiredField.setLanguage("en");
        paymentMethodRequiredField.setImageUrl("https://image.aq/image");
        return paymentMethodRequiredField;
    }

    public static Payment getInvalidPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentId(null);
        payment.setTransactionId(null);
        payment.setPaymentMethod(getInvalidPaymentMethod());
        payment.setAmount(BigDecimal.valueOf(-100.00));
        payment.setCurrencyCode("TEST");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setUpdatedAt(OffsetDateTime.now());
        return payment;
    }

    public static PaymentMethod getInvalidPaymentMethod() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setRequiredFields(getInvalidPaymentMethodRequiredField());
        paymentMethod.setDefinition(getValidPaymentMethodDefinition());
        paymentMethod.setCreatedAt(OffsetDateTime.now());
        paymentMethod.setUpdatedAt(OffsetDateTime.now());
        paymentMethod.setType(null);
        paymentMethod.setLogo("AQ");
        return paymentMethod;
    }

    public static PaymentMethodRequiredField getInvalidPaymentMethodRequiredField() {
        PaymentMethodRequiredField paymentMethodRequiredField = new PaymentMethodRequiredField();
        paymentMethodRequiredField.setId(1L);
        paymentMethodRequiredField.setCreatedAt(OffsetDateTime.now());
        paymentMethodRequiredField.setUpdatedAt(OffsetDateTime.now());
        paymentMethodRequiredField.setCountryAlpha3Code("TEST");
        paymentMethodRequiredField.setCurrencyCode("TEST");
        paymentMethodRequiredField.setName("Test Inc.");
        paymentMethodRequiredField.setDataType("DataType");
        paymentMethodRequiredField.setValidationType("ValidationType");
        paymentMethodRequiredField.setValidationRule("ValidationRule");
        paymentMethodRequiredField.setDefaultValue("DefaultValue");
        paymentMethodRequiredField.setValuesOptions("ValuesOptions");
        paymentMethodRequiredField.setDescription("Description");
        paymentMethodRequiredField.setPlaceholder("Placeholder");
        paymentMethodRequiredField.setRepresentationName("RepresentationName");
        paymentMethodRequiredField.setLanguage("EN");
        paymentMethodRequiredField.setImageUrl("https://image.aq/image");
        return paymentMethodRequiredField;
    }

    public static PaymentMethodDefinition getValidPaymentMethodDefinition() {
        PaymentMethodDefinition paymentMethodDefinition = new PaymentMethodDefinition();
        paymentMethodDefinition.setId(1L);
        return paymentMethodDefinition;
    }

    public static PaymentProviderServiceCreatePaymentResponseDto getValidPaymentProviderServiceCreatePaymentResponseDto() {
        PaymentProviderServiceCreatePaymentResponseDto dto = new PaymentProviderServiceCreatePaymentResponseDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setPaymentId(UUID.randomUUID());
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setPaymentStatus(PaymentStatus.COMPLETED);
        return dto;
    }

    public static PaymentProviderServiceCreatePaymentResponseDto getInvalidPaymentProviderServiceCreatePaymentResponseDto() {
        PaymentProviderServiceCreatePaymentResponseDto dto = new PaymentProviderServiceCreatePaymentResponseDto();
        dto.setTransactionId(null);
        dto.setPaymentId(null);
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setPaymentStatus(PaymentStatus.COMPLETED);
        return dto;
    }
    
    public static PaymentProviderServiceErrorDto getPaymentProviderServiceErrorDto() {
        PaymentProviderServiceErrorDto dto = new PaymentProviderServiceErrorDto();
        dto.setMessage("Error");
        dto.setTraceId(UUID.randomUUID().toString());
        return dto;
    }
}
