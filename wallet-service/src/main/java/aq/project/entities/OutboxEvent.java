package aq.project.entities;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.UnknownOutboxEventPropertyException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Entity
@ToString
@NoArgsConstructor
@Table(name = "outbox_events", schema = "public")
public class OutboxEvent {

    @Id
    @NotNull
    @Getter @Setter
    @Column(name = "transaction_id", nullable = false)
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String transactionId;

    @NotNull
    @Getter @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OperationType operationType;

    @NotNull
    @Getter @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus transactionStatus;

    @Getter @Setter
    @PositiveOrZero
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Getter @Setter
    @Column(name = "processed", nullable = false)
    private boolean isProcessed;

    @Column(name = "property_value")
    @MapKeyColumn(name = "property_key")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "outbox_event_properties", joinColumns = @JoinColumn(name = "outbox_event_transaction_id"))
    private final Map<String, String> properties = new HashMap<>();;

    public OutboxEvent(String transactionId, OperationType operationType, TransactionStatus transactionStatus, Long timestamp, boolean isProcessed) {
        this.transactionId = transactionId;
        this.operationType = operationType;
        this.transactionStatus = transactionStatus;
        this.timestamp = timestamp;
        this.isProcessed = isProcessed;
    }

    public String getProperty(String key) throws UnknownOutboxEventPropertyException {
        if(!isPropertyNull(key))
            return properties.get(key);
        throw new UnknownOutboxEventPropertyException("Unknown outbox event property: " + key);
    }

    public void putProperty(String key, String value) {
        if(key != null && value != null)
            properties.put(key, value);
    }

    public boolean isPropertyNull(String key) {
        return properties.get(key) == null;
    }
}
