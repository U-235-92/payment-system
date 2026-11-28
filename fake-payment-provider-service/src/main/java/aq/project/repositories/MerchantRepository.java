package aq.project.repositories;

import aq.project.entities.Merchant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends CrudRepository<Merchant, UUID> {

    @Query("SELECT m FROM Merchant m WHERE m.merchantId = :merchantId")
    Optional<Merchant> findByMerchantId(String merchantId);
}
