package aq.project.repositories;

import aq.project.entities.Wallet;
import org.springframework.data.repository.CrudRepository;

public interface WalletRepository extends CrudRepository<Wallet, String> {
}
