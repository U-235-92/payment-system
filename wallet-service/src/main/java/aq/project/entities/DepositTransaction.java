package aq.project.entities;

import aq.project.dto.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deposit_transactions", schema = "public")
public class DepositTransaction {

    @Id
    @NotBlank
    @Column(name = "id", nullable = false)
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String id;

    @NotBlank
    @Column(name = "wallet_id", nullable = false)
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String walletId;

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
