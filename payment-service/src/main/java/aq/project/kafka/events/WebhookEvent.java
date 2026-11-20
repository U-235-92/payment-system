package aq.project.kafka.events;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record WebhookEvent(
        @NotNull UUID paymentId,
        @NotNull UUID transactionId,
        @NotNull WebhookEventStatus status,
        @NotNull LocalDateTime timestamp
) {
}
