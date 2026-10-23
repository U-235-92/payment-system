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

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @JoinColumn(name = "country_code", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Country country;

    @NotNull
    @JoinColumn(name = "address_id", nullable = false)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    private Address address;

    @NotNull
    @JoinColumn(name = "individual_id", nullable = false)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    private Individual individual;

    @Embedded
    private InstantEmbeddedData instantEmbeddedData;

    public Person() {
        this.instantEmbeddedData = new InstantEmbeddedData();
    }

    public Person(Person person) {
        id = person.getId();
        keycloakId = person.getKeycloakId();
        firstName =  person.getFirstName();
        lastName =  person.getLastName();
        active = person.getActive();
        instantEmbeddedData = person.getInstantEmbeddedData();

        country = new Country();
        country.setCode(person.getCountry().getCode());
        country.setName(person.getCountry().getName());

        address = new Address();
        address.setId(person.getAddress().getId());
        address.setState(person.getAddress().getState());
        address.setCity(person.getAddress().getCity());
        address.setAddress(person.getAddress().getAddress());
        address.setZip(person.getAddress().getZip());
        address.setInstantEmbeddedData(person.getInstantEmbeddedData());

        individual = new Individual();
        individual.setId(person.getIndividual().getId());
        individual.setEmail(person.getIndividual().getEmail());
        individual.setPassportNumber(person.getIndividual().getPassportNumber());
        individual.setPhoneNumber(person.getIndividual().getPhoneNumber());
        individual.setInstantEmbeddedData(person.getInstantEmbeddedData());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        Person person = (Person) o;

        return Objects.equals(keycloakId, person.keycloakId) &&
                Objects.equals(firstName, person.firstName) &&
                Objects.equals(lastName, person.lastName) &&
                active == person.active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keycloakId, firstName, lastName, active);
    }
}