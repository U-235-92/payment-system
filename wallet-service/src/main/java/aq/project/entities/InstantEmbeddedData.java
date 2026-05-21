package aq.project.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@ToString
@Embeddable
public class InstantEmbeddedData {

    @Column(name = "created", nullable = false)
    private Long createdAt;

    @Column(name = "updated", nullable = false)
    private Long updatedAt;

    public InstantEmbeddedData() {
        super();
        createdAt = Instant.now().toEpochMilli();
        updatedAt = createdAt;
    }

    public void setUpdated(Instant instant) {
        this.updatedAt = Instant.from(instant).toEpochMilli();
    }
}
