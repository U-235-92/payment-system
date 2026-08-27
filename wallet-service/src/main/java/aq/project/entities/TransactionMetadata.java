package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @PositiveOrZero
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;
}
