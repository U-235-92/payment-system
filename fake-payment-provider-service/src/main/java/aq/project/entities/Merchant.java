package aq.project.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "merchants")
public class Merchant extends AbstractEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "merchant_id", length = 50, nullable = false, unique = true)
    private String merchantId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "secret_key", nullable = false)
    private String secretKey;

    @Nullable
    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;
}
