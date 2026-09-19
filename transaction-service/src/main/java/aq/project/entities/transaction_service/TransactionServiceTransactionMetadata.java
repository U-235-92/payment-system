package aq.project.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter @Setter
@NoArgsConstructor
@Table(name = "transaction_metadata")
public class TransactionMetadata {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "trace_id", nullable = false)
    private String traceId;

    @PositiveOrZero
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "notification_url")
    private String notificationUrl;
}
