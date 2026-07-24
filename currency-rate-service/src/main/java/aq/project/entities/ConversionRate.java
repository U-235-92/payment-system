package aq.project.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@ToString
@Getter @Setter
@NoArgsConstructor
@Table(name = "conversion_rates", schema = "public")
public class ConversionRate {

    @Id
    @PositiveOrZero
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "source_iso_code", nullable = false)
    private Currency sourceCurrency;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "destination_iso_code", nullable = false)
    private Currency destinationCurrency;

    @NotNull
    @Column(name = "rate_date", nullable = false)
    private OffsetDateTime rateDate;

    @Column(name = "rate", nullable = false)
    private BigDecimal rate;

    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "provider_rate", nullable = false)
    @MapKeyColumn(name = "provider_code", length = 10)
    @CollectionTable(name = "conversion_providers_rates")
    private Map<String, BigDecimal> providerRateMap;

    public ConversionRate(
            Currency sourceCurrency,
            Currency destinationCurrency,
            OffsetDateTime rateDate,
            BigDecimal rate,
            Map<String, BigDecimal> providerRateMap) {
        this.sourceCurrency = sourceCurrency;
        this.destinationCurrency = destinationCurrency;
        this.rateDate = rateDate;
        this.rate = rate;
        this.providerRateMap = providerRateMap;
    }
}
