package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "countries", schema = "person")
public class Country {

    @Id
    @Column(nullable = false, name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank
    @Size(max = 64)
    @Column(name = "name", nullable = false, length = 64, unique = true)
    private String name;

    @NotBlank
    @Size(max = 3)
    @Column(name = "code", nullable = false, length = 3, unique = true)
    private String code;

    @NotNull
    @Embedded
    private InstantEmbeddedData instantEmbeddedData;
}
