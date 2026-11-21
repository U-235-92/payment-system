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
@Table(name = "verification_callbacks")
public class VerificationCallback extends AbstractEntity {

    @NotBlank
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @NotNull
    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Size(max = 25)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 25)
    private Status operationStatus;

    @Size(max = 255)
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

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