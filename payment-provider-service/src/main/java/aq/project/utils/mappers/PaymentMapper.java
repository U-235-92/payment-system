package aq.project.utils.mappers;

import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "status", expression = "java(toPendingStatus())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toPayment(PaymentProviderServiceCreatePaymentRequestDto dto);

    default PaymentStatus toPendingStatus() {
        return PaymentStatus.PENDING;
    }

    @Mapping(target = "transactionId", expression = "java(toTransactionId(payment))")
    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "paymentStatus", source = "status")
    @Mapping(target = "traceId", ignore = true)
    PaymentProviderServiceCreatePaymentResponseDto toPaymentProviderServiceCreatePaymentResponseDto(Payment payment);

    default UUID toTransactionId(Payment payment) {
        return payment.getTransaction()
                .getId();
    }
}
