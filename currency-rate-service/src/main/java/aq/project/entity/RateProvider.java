package aq.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rate_providers", schema = "public")
public class RateProvider {

    @Id
    @NotBlank
    @Size(max = 10)
    @Column(name = "provider_code", length = 10, nullable = false)
    private String code;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "modified_at")
    private LocalDate modifiedAt;

    @NotBlank
    @Size(max = 255)
    @Column(name = "provider_name", nullable = false, unique = true)
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private boolean isActive;
}
