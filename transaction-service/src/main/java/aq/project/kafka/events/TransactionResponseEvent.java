package aq.project.kafka.events;

import aq.project.dto.PaymentType;
import aq.project.dto.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionResponseEvent(
        @NotNull UUID transactionId,
        @NotNull UUID walletId,
        @NotNull TransactionStatus transactionStatus,
        @NotNull PaymentType paymentType,
        @NotNull @Positive BigDecimal amount,
        String description
) {}
