package aq.project.kafka.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentStatusEvent(
        @NotNull UUID paymentId,
        @NotNull UUID transactionId,
        @NotBlank String status,
        @NotNull LocalDateTime timestamp
) {
}
