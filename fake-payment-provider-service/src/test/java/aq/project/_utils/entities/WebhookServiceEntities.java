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
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = new UpdateStatusOperationRequestDto();

        updateStatusOperationRequestDto.setId(UUID.randomUUID());
        updateStatusOperationRequestDto.setTransactionId(UUID.randomUUID());
        updateStatusOperationRequestDto.setPaymentId(UUID.randomUUID());
        updateStatusOperationRequestDto.setStatus(Status.PROCESSING);
        updateStatusOperationRequestDto.setOperationType(OperationType.TRANSACTION.toString());
        updateStatusOperationRequestDto.setProvider("Provider");
        updateStatusOperationRequestDto.setDescription("Description");
        updateStatusOperationRequestDto.setNotificationUrl("https://example.aq");

        return updateStatusOperationRequestDto;
    }
}