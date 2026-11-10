package aq.project.kafka.events;

import aq.project.dto.PaymentType;
import aq.project.dto.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionResponseEvent(
        UUID transactionId,
        UUID walletId,
        TransactionStatus transactionStatus,
        PaymentType paymentType,
        BigDecimal amount,
        String description
) {}
