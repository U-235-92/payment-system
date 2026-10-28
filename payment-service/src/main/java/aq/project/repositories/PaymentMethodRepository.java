package aq.project.repositories;

import aq.project.entities.PaymentMethod;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PaymentMethodRepository extends CrudRepository<PaymentMethod, Long> {

    @Query(
            "SELECT pm " +
            "FROM PaymentMethod pm " +
            "WHERE pm.requiredFields.currencyCode = :currencyCode " +
            "AND pm.requiredFields.countryAlpha3Code = :countryCode"
    )
    List<PaymentMethod> findAllByCurrencyCodeAndCountryCode(String currencyCode, String countryCode);
}