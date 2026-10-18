package aq.project.utils.mappers;

import aq.project.dto.*;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "status", expression = "java(toPaymentStatus())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toPayment(PaymentServiceCreatePaymentRequestDto dto);

    default PaymentStatus toPaymentStatus() {
        return PaymentStatus.PENDING;
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "paymentStatus", source = "status")
    PaymentServiceCreatePaymentResponseDto toPaymentServiceCreatePaymentResponseDto(Payment payment);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "paymentMethodId", source = "paymentMethodId")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "merchantId", ignore = true)
    PaymentProviderServiceCreatePaymentRequestDto toPaymentProviderServiceCreatePaymentRequestDto(PaymentServiceCreatePaymentRequestDto dto);

    @Mapping(target = "paymentMethodId", source = "id")
    @Mapping(target = "paymentMethodType", source = "type")
    @Mapping(target = "imageUrl", expression = "java(toImageUrl(paymentMethod))")
    @Mapping(target = "description", expression = "java(toDescription(paymentMethod))")
    @Mapping(target = "placeholder", expression = "java(toPlaceholder(paymentMethod))")
    @Mapping(target = "dataType", expression = "java(toDataType(paymentMethod))")
    @Mapping(target = "validationType", expression = "java(toValidationType(paymentMethod))")
    @Mapping(target = "validationRule", expression = "java(toValidationRule(paymentMethod))")
    @Mapping(target = "defaultValue", expression = "java(toDefaultValue(paymentMethod))")
    PaymentServicePaymentMethodResponseDto toPaymentServicePaymentMethodResponseDto(PaymentMethod paymentMethod);

    default String toImageUrl(PaymentMethod paymentMethod) {
        return paymentMethod.getRequiredFields()
                .getImageUrl();
    }

    default String toDescription(PaymentMethod paymentMethod) {
        return paymentMethod.getRequiredFields()
                .getDescription();
    }

    default String toPlaceholder(PaymentMethod paymentMethod) {
        return paymentMethod.getRequiredFields()
                .getPlaceholder();
    }

    default String toDataType(PaymentMethod paymentMethod) {
        return paymentMethod.getRequiredFields()
                .getDataType();
    }

    default String toValidationType(PaymentMethod paymentMethod) {
        return paymentMethod.getRequiredFields()
                .getValidationType();
    }

    default String toValidationRule(PaymentMethod paymentMethod) {
        return paymentMethod.getRequiredFields()
                .getValidationRule();
    }

    default String toDefaultValue(PaymentMethod paymentMethod) {
        return paymentMethod.getRequiredFields()
                .getDefaultValue();
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "merchantId", ignore = true)
    PaymentProviderServiceFailPaymentRequestDto toPaymentProviderServiceFailPaymentRequestDto(
            PaymentServiceCreatePaymentRequestDto dto);
}
