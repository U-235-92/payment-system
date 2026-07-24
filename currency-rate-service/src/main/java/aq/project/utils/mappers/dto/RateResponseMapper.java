package aq.project.utils.mappers.dto;

import aq.project.dto.RateResponse;
import aq.project.entities.ConversionRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class RateResponseMapper {


    @Mapping(target = "sourceCode", expression = "java(toSourceCode(conversionRate))")
    @Mapping(target = "destinationCode", expression = "java(toDestinationCode(conversionRate))")
    @Mapping(target = "rateDate", expression = "java(toRateDate(conversionRate))")
    @Mapping(target = "rate", expression = "java(toRate(conversionRate, provider))")
    @Mapping(target = "providerCode", expression = "java(toProviderCode(conversionRate, provider))")
    public abstract RateResponse toRateResponse(ConversionRate conversionRate, String provider);

    protected String toSourceCode(ConversionRate conversionRate) {
        return conversionRate.getSourceCurrency().getIsoCode();
    }

    protected String toDestinationCode(ConversionRate conversionRate) {
        return conversionRate.getDestinationCurrency().getIsoCode();
    }

    protected OffsetDateTime toRateDate(ConversionRate conversionRate) {
        return conversionRate.getRateDate();
    }

    protected BigDecimal toRate(ConversionRate conversionRate, String provider) {
        if(provider == null || provider.isEmpty() || conversionRate.getProviderRateMap().get(provider) == null)
            return conversionRate.getRate();
        return conversionRate.getProviderRateMap().get(provider);

    }

    protected String toProviderCode(ConversionRate conversionRate, String provider) {
        if(provider == null || provider.isEmpty() || conversionRate.getProviderRateMap().get(provider) == null)
            return null;
        return provider;
    }
}
