package aq.project.entities.wallet;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "wallets", schema = "public")
public class Wallet {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @NotNull
    @JoinColumn(name = "wallet_details_id", nullable = false)
    @OneToOne(cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    private WalletDetails walletDetails;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @NotNull
    @JoinColumn(name = "credit_card_id", nullable = false)
    @OneToOne(cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    private CreditCard creditCard;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
