package aq.project.repositories;

import aq.project.entities.ConversionRate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ConversionRateRepository extends CrudRepository<ConversionRate, Long> {

    @Query(
            "SELECT cr " +
            "FROM ConversionRate cr " +
            "WHERE cr.sourceCurrency.isoCode = :source AND " +
                    "cr.destinationCurrency.isoCode = :destination AND " +
                    "cr.rateDate = :date"
    )
    Optional<ConversionRate> findConversionRate(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("date") OffsetDateTime date
    );

    @Query(
            "SELECT cr " +
            "FROM ConversionRate cr " +
            "WHERE cr.sourceCurrency.isoCode = :source AND " +
                    "cr.destinationCurrency.isoCode = :destination " +
            "ORDER BY cr.rateDate DESC " +
            "LIMIT 1"
    )
    Optional<ConversionRate> findConversionRate(String source, String destination);
}
