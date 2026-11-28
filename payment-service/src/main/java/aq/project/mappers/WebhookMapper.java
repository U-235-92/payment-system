package aq.project.mappers;

import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.exceptions.UnknownOperationStatusException;
import aq.project.kafka.events.WebhookEvent;
import aq.project.kafka.events.WebhookEventStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper
public interface WebhookMapper {

    WebhookMapper INSTANCE = Mappers.getMapper(WebhookMapper.class);

    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "status", expression = "java(toWebhookEventStatus(payment))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp())")
    WebhookEvent toWebhookEvent(Payment payment);

    default WebhookEventStatus toWebhookEventStatus(Payment payment) {
        PaymentStatus paymentStatus = payment.getStatus();

        switch (paymentStatus) {
            case COMPLETED -> {
                return WebhookEventStatus.SUCCESS;
            }

            case FAILED ->  {
                return WebhookEventStatus.FAIL;
            }

            default ->  {
                String exceptionClass = UnknownOperationStatusException.class.getSimpleName();

                UUID paymentId = payment.getPaymentId();

                String msg = String.format(
                        "%s occurred while process [payment] with payment id: [%s]. " +
                                "Received unexpected status: [%s]",
                        exceptionClass, paymentId, paymentStatus);

                throw new UnknownOperationStatusException(msg);
            }
        }
    }

    default LocalDateTime toTimestamp() {
        return LocalDateTime.now();
    }
}