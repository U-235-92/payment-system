package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.Objects;
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

    @NotNull
    @Column(name = "keycloak_id", nullable = false)
    @JoinColumn(name = "id", table = "public.user_entity", nullable = false)
    private String keycloakId;

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

    @Embedded
    private InstantEmbeddedData instantEmbeddedData;

    public Person() {
        this.instantEmbeddedData = new InstantEmbeddedData();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) &&
                Objects.equals(keycloakId, person.keycloakId) &&
                Objects.equals(firstName, person.firstName) &&
                Objects.equals(lastName, person.lastName) &&
                isActive == person.isActive;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, keycloakId, firstName, lastName, isActive);
    }
}