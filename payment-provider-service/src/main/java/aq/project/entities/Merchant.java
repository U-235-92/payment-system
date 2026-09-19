package aq.project.entities;

import aq.project.dto.UserRole;
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
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "merchants")
public class Merchant {

    @Id
    @NotBlank
    @Size(max = 50)
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "secret_key", nullable = false, length = 255)
    private String secretKey;

    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();
}