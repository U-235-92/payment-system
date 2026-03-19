package aq.project.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "service_operations", schema = "service")
public class ServiceOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private long timestamp;

    @Column(name = "operation", nullable = false, updatable = false)
    private String operation;

    @Column(name = "status", nullable = false, updatable = false)
    private String status;

    @Column(name = "description", updatable = false, length = 2048)
    private String description;

    public ServiceOperation() {
        timestamp = System.currentTimeMillis();
    }
}
