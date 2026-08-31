package aq.project.messages.requests;

import aq.project.dto.TransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositTransactionRequest {

    @NotNull
    private UUID transactionId;

    @NotNull
    private UUID walletId;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    private String currency;

    @NotNull
    private TransactionStatus transactionStatus;

    @NotNull
    @PositiveOrZero
    private OffsetDateTime timestamp;

    @NotBlank
    private String traceId;

    @NotNull
    private BigDecimal conversionRate;
}
