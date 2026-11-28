package aq.project.repositories;

import aq.project.entities.WalletOperation;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface WalletOperationRepository extends CrudRepository<WalletOperation, UUID> {

}
