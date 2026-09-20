package aq.project.entities.transaction_service;

import aq.project.dto.TransactionStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Audited
@Getter @Setter
@NoArgsConstructor
@Table(name = "transaction_service_transfer_transactions")
public class TransactionServiceTransferTransaction {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "sender_wallet_id")
    private UUID senderWalletId;

    @NotNull
    @Column(name = "recipient_wallet_id")
    private UUID recipientWalletId;

    @NotNull
    @Positive
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$")
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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @NotNull
    @Column(name = "notification_url", nullable = false)
    private String notificationUrl;

    @Nullable
    @Column(name = "description", length = 2048)
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "metadata_id", nullable = false)
    private TransactionServiceTransactionMetadata transactionMetadata;
}
