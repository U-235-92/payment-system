package aq.project.repository;

import aq.project.entity.ConversionRate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ConversionRateRepository extends CrudRepository<ConversionRate, Long> {

    @Query(
            "SELECT cr " +
            "FROM ConversionRate cr " +
            "WHERE cr.sourceCurrency.isoCode = :source AND " +
                    "cr.destinationCurrency.isoCode = :destination AND " +
                    "cr.rateDate = :date " +
            "ORDER BY cr.id DESC " +
            "LIMIT 1"
    )
    Optional<ConversionRate> findConversionRateBySourceCurrencyAndDestinationCurrency(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("date") LocalDate date
    );
}
