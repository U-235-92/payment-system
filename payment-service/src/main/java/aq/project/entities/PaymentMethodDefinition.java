package aq.project.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_method_definitions")
public class PaymentMethodDefinition {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "is_all_currencies")
    private boolean isAllCurrencies = false;

    @Builder.Default
    @Column(name = "is_all_countries")
    private boolean isAllCountries = false;

    @Builder.Default
    @Column(name = "is_priority")
    private boolean isPriority = false;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;
}