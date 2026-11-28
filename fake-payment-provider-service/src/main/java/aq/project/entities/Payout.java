package aq.project.entities;

import aq.project.dto.Status;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payouts")
public class Payout extends AbstractEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_uuid", nullable = false)
    private Merchant merchant;

    @NotNull
    @Positive
    @Digits(integer = 16, fraction = 2)
    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.PENDING;

    @Nullable
    @Size(max = 100)
    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @Nullable
    @Column(name = "payment_id", unique = true)
    private UUID paymentId;

    @Nullable
    @Size(max = 2048)
    @Column(name = "notification_url", length = 2048)
    private String notificationUrl;

    public void complete() {
        this.status = Status.SUCCESS;
    }

    public void fail() {
        this.status = Status.FAIL;
    }

    public void processing() {
        this.status = Status.PROCESSING;
    }
}
