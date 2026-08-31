package aq.project.messages.responses;

import aq.project.dto.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferTransactionResponse {

    @NotNull
    private UUID transactionId;

    @NotNull
    private UUID senderWalletId;

    @NotNull
    private UUID recipientWalletId;

    @NotNull
    private TransactionStatus transactionStatus;

    @NotNull
    private OffsetDateTime timestamp;
}
