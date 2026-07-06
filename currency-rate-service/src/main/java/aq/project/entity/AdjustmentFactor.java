package aq.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@Table(name = "adjustment_factors")
public class AdjustmentFactor {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @OneToOne
    @JoinColumn(name = "provider_code", nullable = false)
    private RateProvider rateProvider;

    @NotNull
    @Column(name = "factor", nullable = false)
    private BigDecimal factor;

    @PositiveOrZero
    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @PositiveOrZero
    @Column(name = "modified_at")
    private long modifiedAt;
}
