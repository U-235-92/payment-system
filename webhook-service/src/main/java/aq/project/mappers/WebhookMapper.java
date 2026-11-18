package aq.project.mappers;

import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.PaymentProviderCallback;
import aq.project.entities.UnknownCallback;
import aq.project.kafka.events.WebhookEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.ObjectMapper;

@Mapper
public interface WebhookMapper {

    WebhookMapper INSTANCE = Mappers.getMapper(WebhookMapper.class);

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "status", source = "targetWebhookStatus")
    @Mapping(target = "timestamp", ignore = true)
    WebhookEvent toWebhookEvent(PaymentProviderCallback paymentProviderCallback);

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
    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "provider", source = "provider")
    PaymentProviderCallback toPaymentProviderCallback(UpdateStatusOperationRequestDto dto);

    default String toCallbackBody(UpdateStatusOperationRequestDto dto) {
        return OBJECT_MAPPER.writeValueAsString(dto);
    }
}
