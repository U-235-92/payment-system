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
@Table(name = "deposit_transactions", schema = "public")
public class DepositTransaction {

    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

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
