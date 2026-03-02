package aq.project.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Embeddable
public class InstantEmbeddedData {

    @NotNull
    @Column(name = "created", nullable = false)
    private Instant created;

    @NotNull
    @Column(name = "updated", nullable = false)
    private Instant updated;

    public InstantEmbeddedData() {
        super();
        created = Instant.now();
        updated = Instant.from(created);
    }
}
