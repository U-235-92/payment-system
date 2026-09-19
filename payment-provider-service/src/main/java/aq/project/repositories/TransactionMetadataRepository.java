package aq.project.repositories;

import aq.project.entities.TransactionMetadata;
import org.springframework.data.repository.CrudRepository;

public interface TransactionMetadataRepository extends CrudRepository<TransactionMetadata, Long> {
}
