package aq.project._utils.entities;

import aq.project.dto.PaymentServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServicePaymentMethodResponseDto;
import aq.project.dto.PaymentStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PaymentServiceEntities {

    public static PaymentServicePaymentMethodResponseDto getValidPaymentServicePaymentMethodResponseDto() {
        PaymentServicePaymentMethodResponseDto dto = new PaymentServicePaymentMethodResponseDto();
        dto.setPaymentMethodId(1L);
        dto.setPaymentMethodType(PaymentServicePaymentMethodResponseDto.PaymentMethodTypeEnum.BANK_TRANSFER);
        dto.setImageUrl("https://www.image.aq");
        dto.setDescription("Description");
        dto.setPlaceholder("Placeholder");
        dto.setDataType("DataType");
        dto.setValidationType("ValidationType");
        dto.setValidationRule("ValidationRule");
        dto.setDefaultValue("DefaultValue");
        return dto;
    }

    public static PaymentServiceCreatePaymentResponseDto getValidPaymentServiceCreatePaymentResponseDto() {
        PaymentServiceCreatePaymentResponseDto dto = new PaymentServiceCreatePaymentResponseDto();
        dto.setPaymentId(UUID.randomUUID());
        dto.setTransactionId(UUID.randomUUID());
        dto.setPaymentStatus(PaymentStatus.COMPLETED);
        return dto;
    }
}
