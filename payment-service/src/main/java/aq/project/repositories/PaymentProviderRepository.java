package aq.project.repositories;

import aq.project.entities.PaymentProvider;
import org.springframework.data.repository.CrudRepository;

public interface PaymentProviderRepository extends CrudRepository<PaymentProvider, Long> {
}