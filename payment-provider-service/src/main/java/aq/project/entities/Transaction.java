package aq.project.entities;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Audited
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @NotNull
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false, referencedColumnName = "id")
    private Merchant merchant;

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "999999999999.99")
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]{3}$")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private OperationType operationType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    @Size(max = 100)
    @Column(name = "external_id", length = 100)
    private String externalId;

    @Size(max = 2048)
    @Column(name = "notification_url", length = 2048)
    private String notificationUrl;
}