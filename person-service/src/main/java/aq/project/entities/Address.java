package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "addresses", schema = "person")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @Embedded
    private InstantEmbeddedData instantEmbeddedData;

    public Address() {
        this.instantEmbeddedData = new InstantEmbeddedData();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        Address address1 = (Address) o;
        return Objects.equals(state, address1.state) &&
                Objects.equals(city, address1.city) &&
                Objects.equals(address, address1.address) &&
                Objects.equals(zip, address1.zip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, city, address, zip);
    }
}
