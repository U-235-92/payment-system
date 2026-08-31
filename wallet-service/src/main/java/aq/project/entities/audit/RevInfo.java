package aq.project.entities.audit;

import aq.project.utils.audit.ServiceRevisionListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Getter @Setter
@RevisionEntity(ServiceRevisionListener.class)
@Table(name="revinfo", schema = "public")
public class RevInfo {

    @Id
    @RevisionNumber
    @Column(name = "rev")
    private long rev;

    @RevisionTimestamp
    @Column(name = "revtstmp")
    private long revtstmp;
}
