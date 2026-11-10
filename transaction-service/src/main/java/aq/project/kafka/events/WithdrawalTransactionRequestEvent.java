package aq.project.kafka.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WithdrawalTransactionRequestEvent(
        UUID transactionId,
        UUID userId,
        UUID walletId,
        UUID targetWalletId,
        BigDecimal amount,
        String currency,
        LocalDateTime timestamp
) {}
