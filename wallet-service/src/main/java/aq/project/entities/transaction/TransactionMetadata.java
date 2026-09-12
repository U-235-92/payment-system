package aq.project.entities.transaction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction_metadata", schema = "public")
public class TransactionMetadata {

    @Id
    @Positive
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "trace_id", nullable = false)
    private String traceId;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;
}
