package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;

@Entity
@Audited
@Getter @Setter
@NoArgsConstructor
@Table(name = "transaction_metadata")
public class TransactionMetadata {

    @Id
    @Positive
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "timestamp", updatable = false, nullable = false)
    private OffsetDateTime timestamp;

    @NotBlank
    @Column(name = "trace_id", nullable = false)
    private String traceId;
}
