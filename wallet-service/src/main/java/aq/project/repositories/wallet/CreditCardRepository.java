package aq.project.repositories.wallet;

import aq.project.entities.wallet.CreditCard;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CreditCardRepository extends CrudRepository<CreditCard, UUID> {
}
