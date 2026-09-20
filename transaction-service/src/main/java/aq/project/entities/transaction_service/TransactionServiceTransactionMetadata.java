package aq.project.entities.transaction_service;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;

@Entity
@Audited
@Getter @Setter
@NoArgsConstructor
@Table(name = "transaction_service_transaction_metadata")
public class TransactionServiceTransactionMetadata {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "trace_id", nullable = false)
    private String traceId;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;
}
