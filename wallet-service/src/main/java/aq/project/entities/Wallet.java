package aq.project.entities;

import aq.project.dto.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallets")
public class Wallet extends AbstractEntity {

    @Column(name = "name", nullable = false, length = 32)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "wallet_type_id")
    private WalletType walletType;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WalletStatus walletStatus = WalletStatus.ACTIVE;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        if (balance.compareTo(amount) < 0)
            throw new IllegalStateException("Insufficient balance of wallet");

        balance = balance.subtract(amount);
    }

    public boolean isActive() {
        return walletStatus == WalletStatus.ACTIVE;
    }

    public boolean isBlocked() {
        return walletStatus == WalletStatus.BLOCKED;
    }

    public boolean isBalanceExceedAmount(BigDecimal amount) {
        return amount != null && balance.compareTo(amount) >= 0;
    }
}
