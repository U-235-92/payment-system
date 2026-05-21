package aq.project.entities;

import aq.project.dto.WalletStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

@Entity
@Audited
@ToString
@Getter @Setter
@Table(name = "wallet_details", schema = "public")
public class WalletDetails {

    @Id
    @Column(name = "id")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String id;

    @Column(name = "wallet_id", updatable = false)
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String walletId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "creator", nullable = false)
    private String creator;

    @NotBlank
    @Size(max = 255)
    @Column(name = "modifier", nullable = false)
    private String modifier;

    @PositiveOrZero
    @Column(name = "archived_at")
    private Long archivedAt;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="wallet_status", nullable = false, length = 18)
    private WalletStatus walletStatus;

    @NotNull
    @Embedded
    private InstantEmbeddedData instantEmbeddedData;
}
