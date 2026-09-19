package aq.project.entities.transaction;

import aq.project.dto.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transfer_transactions", schema = "public")
public class TransferTransaction {

    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "sender_wallet_id", nullable = false)
    private UUID senderWalletId;

    @NotNull
    @Column(name = "recipient_wallet_id", nullable = false)
    private UUID recipientWalletId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "processed", nullable = false)
    private boolean isProcessed;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "metadata", nullable = false)
    private TransactionMetadata metadata;
}
