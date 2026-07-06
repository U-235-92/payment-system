package aq.project.repository;

import aq.project.entity.AdjustmentFactor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdjustmentFactorRepository extends CrudRepository<AdjustmentFactor, Long> {

    @Query("SELECT af FROM AdjustmentFactor af WHERE af.rateProvider.code = :rateProviderCode")
    Optional<AdjustmentFactor> findByRateProviderCode(String rateProviderCode);
}
