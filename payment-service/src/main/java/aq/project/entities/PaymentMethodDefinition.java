package aq.project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_method_definitions")
public class PaymentMethodDefinition {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_all_currencies")
    private boolean isAllCurrencies = false;

    @Column(name = "is_all_countries")
    private boolean isAllCountries = false;

    @Column(name = "is_priority")
    private boolean isPriority = false;

    @Column(name = "is_active")
    private boolean isActive = true;
}