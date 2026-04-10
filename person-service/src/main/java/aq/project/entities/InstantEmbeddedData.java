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
    private long created;

    @Column(name = "updated", nullable = false)
    private long updated;

    public InstantEmbeddedData() {
        super();
        created = Instant.now().toEpochMilli();
        updated = created;
    }

    public void setUpdated(Instant instant) {
        this.updated = Instant.from(instant).toEpochMilli();
    }
}
