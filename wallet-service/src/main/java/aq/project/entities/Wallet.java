package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

@Entity
@Audited
@ToString
@Getter @Setter
@Table(name = "wallets", schema = "public")
public class Wallet {

    @Id
    @Column(name = "id", nullable = false)
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String id;

    @NotBlank
    @Column(name = "person_id", nullable = false)
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String personId;

    @NotNull
    @JoinColumn(name = "wallet_details_id", nullable = false)
    @OneToOne(cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    private WalletDetails walletDetails;

    @PositiveOrZero
    @Column(name = "archived_at")
    private Long archivedAt;

    @NotNull
    @JoinColumn(name = "credit_card_id", nullable = false)
    @OneToOne(cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    private CreditCard creditCard;

    @NotNull
    @Embedded
    private InstantEmbeddedData instantEmbeddedData;
}
