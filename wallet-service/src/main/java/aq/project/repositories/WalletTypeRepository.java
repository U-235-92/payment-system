package aq.project.repositories;

import aq.project.entities.WalletType;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface WalletTypeRepository extends CrudRepository<WalletType, UUID> {
}
