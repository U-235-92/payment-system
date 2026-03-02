package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "individuals", schema = "person")
public class Individual {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 1024)
    @Column(name = "email", nullable = false, unique = true, length = 1024)
    private String email;

    @NotBlank
    @Size(max = 32)
    @Column(name = "passport_number", nullable = false, unique = true, length = 32)
    private String passportNumber;

    @NotBlank
    @Size(max = 32)
    @Column(name = "phone_number", nullable = false, unique = true, length = 32)
    private String phoneNumber;

    @NotNull
    @Embedded
    private InstantEmbeddedData instantEmbeddedData;
}
