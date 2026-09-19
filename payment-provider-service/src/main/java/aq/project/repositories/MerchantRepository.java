package aq.project.repositories;

import aq.project.entities.Merchant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends CrudRepository<Merchant, String> {

    boolean existsById(String id);
}