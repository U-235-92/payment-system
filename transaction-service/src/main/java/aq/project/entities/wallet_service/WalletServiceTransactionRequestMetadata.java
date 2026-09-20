package aq.project.entities.wallet_service;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "wallet_service_transaction_request_metadata")
public class WalletServiceTransactionRequestMetadata {

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

    @Column(name = "is_processed", nullable = false)
    private boolean isProcessed;
}
