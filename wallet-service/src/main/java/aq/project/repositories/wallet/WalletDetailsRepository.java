package aq.project.repositories.wallet;

import aq.project.entities.wallet.WalletDetails;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface WalletDetailsRepository extends CrudRepository<WalletDetails, UUID> {
}
