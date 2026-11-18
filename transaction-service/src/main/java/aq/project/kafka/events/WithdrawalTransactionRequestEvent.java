package aq.project.kafka.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WithdrawalTransactionRequestEvent(
        @NotNull UUID transactionId,
        @NotNull UUID userId,
        @NotNull UUID walletId,
        @NotNull UUID targetWalletId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}]") String currency,
        @NotNull LocalDateTime timestamp
) {}
