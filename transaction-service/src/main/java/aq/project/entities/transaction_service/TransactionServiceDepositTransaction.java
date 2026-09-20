package aq.project.entities.transaction_service;

import aq.project.dto.TransactionStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@Table(name = "transaction_service_deposit_transactions")
public class TransactionServiceDepositTransaction {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "wallet_id")
    private UUID walletId;

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
    @Column(name = "conversion_rate", nullable = false)
    private BigDecimal conversionRate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @NotBlank
    @Size(max = 2048)
    @Column(name = "notification_url", nullable = false, length = 2048)
    private String notificationUrl;

    @Nullable
    @Column(name = "description", length = 2048)
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "metadata_id", nullable = false)
    private TransactionServiceTransactionMetadata transactionMetadata;
}
