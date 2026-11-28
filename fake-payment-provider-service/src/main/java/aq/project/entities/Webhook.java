package aq.project.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Builder()
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "webhooks")
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "event_type", length = 50, nullable = false)
    private String operationType;

    @NotNull
    @Column(name = "entity_uuid")
    private UUID entityId;

    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload")
    private String payload;

    @CreationTimestamp
    @Column(name = "received_at", updatable = false)
    private OffsetDateTime receivedAt;

    @Nullable
    @Size(max = 2048)
    @Column(name = "notification_url", length = 2048)
    private String notificationUrl;

    @NotNull
    @Column(name = "processed", nullable = false)
    private Boolean processed;

    public void processed() {
        this.processed = true;
    }
}
