package aq.project.entities;

import aq.project.dto.PaymentType;
import aq.project.dto.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction extends AbstractEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "target_wallet_id", nullable = false)
    private UUID targetWalletId;

    @PositiveOrZero
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Pattern(regexp = "[A-Z]{3}")
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private PaymentType paymentType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TransactionStatus transactionStatus = TransactionStatus.PENDING;

    @Column(name = "description", length = 256)
    private String description;

    @Builder.Default
    @Column(name = "fee")
    private BigDecimal fee = BigDecimal.ZERO;

    public boolean isDeposit() {
        return paymentType == PaymentType.DEPOSIT;
    }

    public boolean isTransfer() {
        return paymentType == PaymentType.TRANSFER;
    }

    public boolean isWithdrawal() {
        return paymentType == PaymentType.WITHDRAWAL;
    }

    public void failTransaction() {
        transactionStatus = TransactionStatus.FAILED;
    }

    public void completeTransaction() {
        transactionStatus = TransactionStatus.COMPLETED;
    }

    public BigDecimal getTotalAmount() {
        if(fee != null)
            return amount.add(fee);
        return amount;
    }
}
