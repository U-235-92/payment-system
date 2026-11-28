package aq.project.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "unknown_callbacks")
public class UnknownCallback extends AbstractEntity {

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

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
