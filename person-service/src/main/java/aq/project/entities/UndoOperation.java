package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "undo_operations", schema = "service")
public class UndoOperation {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "person_keycloak_id")
    private UUID personKeycloakId;

    @NotNull
    @Column(name = "operation", nullable = false)
    private String operation;

    @Positive
    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    @Column(name = "description", length = 2048)
    private String description;
}
