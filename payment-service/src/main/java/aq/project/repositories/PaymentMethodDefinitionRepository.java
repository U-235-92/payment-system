package aq.project.repositories;

import aq.project.entities.PaymentMethodDefinition;
import org.springframework.data.repository.CrudRepository;

public interface PaymentMethodDefinitionRepository extends CrudRepository<PaymentMethodDefinition, Long> {
}