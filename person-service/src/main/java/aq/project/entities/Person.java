package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Entity
@Getter
@Setter
@Audited
@ToString
@Table(name = "users", schema = "person")
public class Person {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 32)
    @Column(name = "first_name", nullable = false, length = 32)
    private String firstName;

    @NotBlank
    @Size(max = 32)
    @Column(name = "last_name", nullable = false, length = 32)
    private String lastName;

    @Column(name = "active", nullable = false)
    private boolean isActive;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "individual_id", nullable = false)
    private Individual individual;

    @NotNull
    @Embedded
    private InstantEmbeddedData instantEmbeddedData;
}