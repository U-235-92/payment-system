package aq.project.entities;

import aq.project.dto.OperationType;
import aq.project.dto.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_provider_callbacks")
public class PaymentProviderCallback extends AbstractEntity {

    @NotBlank
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @NotNull
    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @NotNull
    @Column(name = "payment_id", nullable = false, unique = true)
    private UUID paymentId;

    @Size(max = 255)
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Size(max = 255)
    @Column(name = "provider")
    private String provider;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_webhook_status", nullable = false, updatable = false)
    private Status targetWebhookStatus;

    @NotNull
    @Column(name = "processed", nullable = false)
    private Boolean processed;

    public boolean isProcessed() {
        return this.processed == true;
    }

    public void markProcessed() {
        this.processed = true;
    }
}
