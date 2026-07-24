package aq.project.utils.mappers.rate;

import aq.project.entities.ConversionRate;
import aq.project.entities.Currency;
import aq.project.entities.RateProvider;

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
