package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Table(
        name = "payment_method_required_fields",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "country_alpha3_code", "currency_code", "name", "language" }
                )
        }
)
public class PaymentMethodRequiredField {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]{3}$")
    @Column(name = "country_alpha3_code", length = 3)
    private String countryAlpha3Code;

    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]{3}$")
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @NotBlank
    @Size(max = 128)
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @NotBlank
    @Size(max = 128)
    @Column(name = "data_type", nullable = false, length = 128)
    private String dataType;

    @Size(max = 128)
    @Column(name = "validation_type", length = 128)
    private String validationType;

    @Size(max = 256)
    @Column(name = "validation_rule", length = 256)
    private String validationRule;

    @Size(max = 128)
    @Column(name = "default_value", length = 128)
    private String defaultValue;

    @Column(name = "values_options", columnDefinition = "TEXT")
    private String valuesOptions;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    @Size(max = 255)
    @Column(name = "placeholder", length = 255)
    private String placeholder;

    @Size(max = 255)
    @Column(name = "representation_name", length = 255)
    private String representationName;

    @Size(min = 2, max = 2)
    @Pattern(regexp = "^[a-z]{2}$")
    @Column(name = "language", length = 2)
    private String language;

    @Size(max = 2048)
    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "is_active")
    private boolean isActive = true;
}