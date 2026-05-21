package aq.project.repositories;

import aq.project.entities.CreditCard;
import org.springframework.data.repository.CrudRepository;

public interface CreditCardRepository extends CrudRepository<CreditCard, String> {
}
