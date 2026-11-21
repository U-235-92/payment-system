package aq.project.mappers;

import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.PaymentProviderCallback;
import aq.project.entities.UnknownCallback;
import aq.project.exceptions.UnknownOperationStatusException;
import aq.project.kafka.events.WebhookEvent;
import aq.project.kafka.events.WebhookEventStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper
public interface WebhookMapper {

    WebhookMapper INSTANCE = Mappers.getMapper(WebhookMapper.class);

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "status", expression = "java(toWebhookEventStatus(paymentProviderCallback))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp())")
    WebhookEvent toWebhookEvent(PaymentProviderCallback paymentProviderCallback);

    default WebhookEventStatus toWebhookEventStatus(PaymentProviderCallback paymentProviderCallback) {
        Status callbackStatus = paymentProviderCallback.getTargetWebhookStatus();

        switch (callbackStatus) {
            case SUCCESS -> {
                return WebhookEventStatus.SUCCESS;
            }

            case FAIL ->  {
                return WebhookEventStatus.FAIL;
            }

            default ->  {
                String exceptionClass = UnknownOperationStatusException.class.getSimpleName();

                UUID callbackId = paymentProviderCallback.getId();

                String msg = String.format(
                        "%s occurred while process [payment provider callback] with id: [%s]. " +
                                "Received unexpected status: [%s]",
                        exceptionClass, callbackId, callbackStatus);

                throw new UnknownOperationStatusException(msg);
            }
        }
    }

    default LocalDateTime toTimestamp() {
        return LocalDateTime.now();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "body", expression = "java(toCallbackBody(dto))")
    UnknownCallback toUnknownCallbackRepository(UpdateStatusOperationRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "body", expression = "java(toCallbackBody(dto))")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "targetWebhookStatus", source = "status")
    PaymentProviderCallback toPaymentProviderCallback(UpdateStatusOperationRequestDto dto);

    default String toCallbackBody(UpdateStatusOperationRequestDto dto) {
        return OBJECT_MAPPER.writeValueAsString(dto);
    }
}