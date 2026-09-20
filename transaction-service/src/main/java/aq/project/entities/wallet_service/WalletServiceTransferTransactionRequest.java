package aq.project.entities.wallet_service;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "wallet_service_transfer_transaction_requests")
public class WalletServiceTransferTransactionRequest {

    @Id
    @NotNull
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @NotNull
    @Column(name = "sender_wallet_id", nullable = false)
    private UUID senderWalletId;

    @NotNull
    @Column(name = "recipient_wallet_id", nullable = false)
    private UUID recipientWalletId;

    @NotNull
    @Positive
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotBlank
    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @NotNull
    @Positive
    @Column(name = "sender_conversion_rate", nullable = false)
    private BigDecimal senderConversionRate;

    @NotNull
    @Positive
    @Column(name = "recipient_conversion_rate", nullable = false)
    private BigDecimal recipientConversionRate;

    @JoinColumn(name = "metadata_id")
    @OneToOne(cascade = CascadeType.ALL)
    private WalletServiceTransactionRequestMetadata transactionRequestMetadata;
}
