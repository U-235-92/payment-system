package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.Objects;

@Entity
@Getter
@Setter
@Audited
@ToString
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

    @Embedded
    private InstantEmbeddedData instantEmbeddedData;

    public Country() {
        this.instantEmbeddedData = new InstantEmbeddedData();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(name, country.name) &&
                Objects.equals(code, country.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code);
    }
}
