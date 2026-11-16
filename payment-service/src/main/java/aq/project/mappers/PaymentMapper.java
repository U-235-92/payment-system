package aq.project.mappers;

import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServicePaymentMethodResponseDto;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(target = "paymentMethodId", source = "id")
    @Mapping(target = "paymentMethodType", source = "type")
    @Mapping(target = "imageUrl", source = "requiredFields.imageUrl")
    @Mapping(target = "description", source = "requiredFields.description")
    @Mapping(target = "placeholder", source = "requiredFields.placeholder")
    @Mapping(target = "dataType", source = "requiredFields.dataType")
    @Mapping(target = "validationType", source = "requiredFields.validationType")
    @Mapping(target = "validationRule", source = "requiredFields.validationRule")
    @Mapping(target = "defaultValue", source = "requiredFields.defaultValue")
    PaymentServicePaymentMethodResponseDto toPaymentServicePaymentMethodResponseDto(PaymentMethod paymentMethod);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toPayment(PaymentServiceCreatePaymentRequestDto dto);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "paymentStatus", source = "status")
    PaymentServiceCreatePaymentResponseDto toPaymentServiceCreatePaymentResponseDto(Payment payment);

    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "externalId", source = "transactionId")
    @Mapping(target = "notificationUrl", ignore = true)
    CreatePayoutRequestDto toCreatePayoutRequestDto(Payment payment);
}