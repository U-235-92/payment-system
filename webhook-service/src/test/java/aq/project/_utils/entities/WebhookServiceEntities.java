package aq.project._utils.entities;

import aq.project.dto.OperationType;
import aq.project.dto.Status;
import aq.project.dto.UpdateStatusOperationRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class WebhookServiceEntities {

    public static UpdateStatusOperationRequestDto getUpdateStatusOperationRequestDto() {
        return UpdateStatusOperationRequestDto.builder()
                .id(UUID.randomUUID())
                .transactionId(UUID.randomUUID())
                .paymentId(UUID.randomUUID())
                .status(Status.SUCCESS)
                .operationType(OperationType.TRANSACTION.toString())
                .provider("provider")
                .description("description")
                .notificationUrl("https://example.aq")
                .build();
    }
}