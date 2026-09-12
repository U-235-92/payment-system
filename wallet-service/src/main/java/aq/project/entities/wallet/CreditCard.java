package aq.project.entities.wallet;

import aq.project.dto.CardType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Audited
@ToString
@Getter @Setter
@Table(name = "credit_cards", schema = "public")
public class CreditCard {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "wallet_id", updatable = false)
    private UUID walletId;

    @NotBlank
    @Column(name = "number", nullable = false)
    @Pattern(regexp = "^[0-9]{4}\\s[0-9]{4}\\s[0-9]{4}\\s[0-9]{4}$")
    private String cardNumber;

    @NotBlank
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[0-9]{3}$")
    @Column(name = "cvv", nullable = false, length = 3)
    private String cardCvvNumber;

    @NotNull
    @Column(name = "expiration_date", nullable = false)
    private YearMonth cardExpirationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private CardType cardType;

    @NotNull
    @Column(name = "balance", precision = 14, scale = 2, nullable = false)
    private BigDecimal balance;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

