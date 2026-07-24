package aq.project.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "currencies", schema = "public")
public class Currency {

    @Id
    @NotBlank
    @Size(max = 3)
    @Column(name = "iso_code", nullable = false, length = 3)
    private String isoCode;

    @PositiveOrZero
    @Column(name = "iso_numeric", nullable = false)
    private int isoNumeric;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    @NotBlank
    @Size(max = 64)
    @Column(name = "description", nullable = false, length = 64)
    private String description;

    @Column(name = "active")
    private boolean isActive;

    @NotBlank
    @Size(max = 10)
    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;
}
