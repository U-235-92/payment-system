package aq.project.entities;

import aq.project.dto.PaymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_method_required_fields_id")
    private PaymentMethodRequiredField requiredFields;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_method_definitions_id")
    private PaymentMethodDefinition definition;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 64)
    private PaymentType type;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "logo", columnDefinition = "TEXT")
    private String logo;

    @NotBlank
    @Size(max = 24)
    @Column(name = "profile_type", nullable = false, length = 24)
    private String profileType = "INDIVIDUAL";
}