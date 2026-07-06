package aq.project.mappers.rate;

import aq.project.entity.AdjustmentFactor;
import aq.project.entity.ConversionRate;
import aq.project.entity.Currency;
import aq.project.entity.RateProvider;

import java.util.List;
import java.util.Map;

public interface AbstractRateProviderMapper {

    default Map<String, RateProvider> getRateProvidersMap(String rateClientResponse) throws Exception {
        throw new UnsupportedOperationException();
    }

    default Map<String, Currency> getCurrencyMap(String rateClientResponse) throws Exception {
        throw new UnsupportedOperationException();
    }

    default List<ConversionRate> getConversionRateList(
            String rateClientResponse,
            Map<String, Currency> currenciesMap
    ) throws Exception {
        throw new UnsupportedOperationException();
    }
}
