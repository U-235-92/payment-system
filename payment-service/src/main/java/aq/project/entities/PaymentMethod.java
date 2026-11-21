package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_methods")
@NamedEntityGraph(
        name = "payment_method_entity_graph",
        attributeNodes = {
                @NamedAttributeNode("requiredFields"),
                @NamedAttributeNode("definition")
        }
)
public class PaymentMethod {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "payment_method_required_fields_id")
    private PaymentMethodRequiredField requiredFields;

    @OneToOne
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
    @Column(name = "method", nullable = false, length = 64)
    private aq.project.dto.PaymentMethod method;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "logo", columnDefinition = "TEXT")
    private String logo;

    @NotBlank
    @Size(max = 24)
    @Builder.Default
    @Column(name = "profile_type", nullable = false, length = 24)
    private String profileType = "INDIVIDUAL";
}