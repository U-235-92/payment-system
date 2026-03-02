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
@Table(name = "addresses", schema = "person")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @JoinColumn(name = "country_id", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Country country;

    @NotBlank
    @Size(max = 64)
    @Column(name = "state", nullable = false, length = 64)
    private String state;

    @NotBlank
    @Size(max = 64)
    @Column(name = "city", nullable = false, length = 64)
    private String city;

    @NotBlank
    @Size(max = 128)
    @Column(name = "address", nullable = false, length = 128)
    private String address;

    @NotBlank
    @Size(max = 32)
    @Column(name = "zip_code", nullable = false, length = 32)
    private String zip;

    @NotNull
    @Embedded
    private InstantEmbeddedData instantEmbeddedData;
}
