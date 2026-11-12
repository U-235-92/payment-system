package aq.project.entities;

import aq.project.dto.PaymentMethod;
import aq.project.dto.Status;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction extends AbstractEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
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
    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 50, nullable = false)
    private PaymentMethod paymentMethod;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.PENDING;

    @Nullable
    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Nullable
    @Size(max = 100)
    @Column(name = "external_id", length = 100)
    private String externalId;

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
}
