package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "undo_events", schema = "service")
public class UndoEvent {

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

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    @NotNull
    @Column(name = "description", nullable = false,  length = 2048)
    private String description;
}
