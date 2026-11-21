package aq.project.entities;

import aq.project.dto.PaymentType;
import aq.project.dto.TransactionStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction extends AbstractEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "wallet_id", nullable = false, updatable = false)
//    Marked as [updatable = false] because this column is participant of shard process.
//    If you remove this declaration you will get Exception [could not execute statement].
//    You MUST mark sharding columns as [updatable = false].
    private UUID walletId;

    @Nullable
    @Column(name = "target_wallet_id")
    private UUID targetWalletId;

    @NotNull
    @PositiveOrZero
    @Column(name = "wallet_amount", nullable = false)
    private BigDecimal walletAmount;

    @Nullable
    @PositiveOrZero
    @Column(name = "target_wallet_amount")
    private BigDecimal targetWalletAmount;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private PaymentType paymentType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TransactionStatus transactionStatus = TransactionStatus.PENDING;

    @Nullable
    @Column(name = "description", length = 256)
    private String description;

    @Nullable
    @Column(name = "fee")
    private BigDecimal fee = BigDecimal.ZERO;

    public void failTransaction() {
        transactionStatus = TransactionStatus.FAILED;
    }

    public void completeTransaction() {
        transactionStatus = TransactionStatus.COMPLETED;
    }
}