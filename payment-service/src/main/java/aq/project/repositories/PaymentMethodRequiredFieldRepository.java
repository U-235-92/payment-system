package aq.project.repositories;

import aq.project.entities.PaymentMethodRequiredField;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PaymentMethodRequiredFieldRepository extends CrudRepository<PaymentMethodRequiredField, UUID> {
}