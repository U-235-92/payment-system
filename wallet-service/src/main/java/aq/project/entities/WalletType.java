package aq.project.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet_types")
public class WalletType extends AbstractEntity {

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Size(max = 15)
    @Column(name = "user_type", length = 15)
    private String userType;

    @Column(name = "creator")
    private String creator;

    @Column(name = "modifier")
    private String modifier;
}