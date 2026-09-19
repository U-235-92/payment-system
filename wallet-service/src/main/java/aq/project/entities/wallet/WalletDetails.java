package aq.project.entities.wallet;

import aq.project.dto.WalletStatus;
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

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Audited
@ToString
@Getter @Setter
@Table(name = "wallet_details", schema = "public")
public class WalletDetails {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "wallet_id", updatable = false)
    private UUID walletId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "creator", nullable = false)
    private String creator;

    @NotBlank
    @Size(max = 255)
    @Column(name = "modifier", nullable = false)
    private String modifier;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="wallet_status", nullable = false, length = 18)
    private WalletStatus walletStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
