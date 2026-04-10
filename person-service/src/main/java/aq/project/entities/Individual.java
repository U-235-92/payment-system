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
@Table(name = "individuals", schema = "person")
public class Individual {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 1024)
    @Column(name = "email", nullable = false, length = 1024)
    private String email;

    @NotBlank
    @Size(max = 32)
    @Column(name = "passport_number", nullable = false, length = 32)
    private String passportNumber;

    @NotBlank
    @Size(max = 32)
    @Column(name = "phone_number", nullable = false, length = 32)
    private String phoneNumber;

    @Embedded
    private InstantEmbeddedData instantEmbeddedData;

    public Individual() {
        this.instantEmbeddedData = new InstantEmbeddedData();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Individual that = (Individual) o;
        return Objects.equals(email, that.email) &&
                Objects.equals(passportNumber, that.passportNumber) &&
                Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, passportNumber, phoneNumber);
    }
}
