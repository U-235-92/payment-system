package aq.project.entities;

import aq.project.dto.PersonServiceOperation;
import aq.project.dto.UndoOperationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "service_operations", schema = "service")
public class ServiceOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, updatable = false)
    private PersonServiceOperation serviceOperation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, updatable = false)
    private UndoOperationStatus status;

    @Column(name = "description", updatable = false, length = 2048)
    private String description;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private OffsetDateTime timestamp;
}
