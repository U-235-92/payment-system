package aq.project.mappers.dto;

import aq.project.dto.RateResponse;
import aq.project.entity.ConversionRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import static aq.project.util.constants.CustomConstants.*;

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

    protected String toRateDate(ConversionRate conversionRate) {
        return conversionRate.getRateDate().format(DateTimeFormatter.ofPattern(ISO_DATE_FORMAT));
    }

    protected BigDecimal toRate(ConversionRate conversionRate, String provider) {
        if(provider == null || provider.isEmpty() || conversionRate.getProviderRateMap().get(provider) == null)
            return BigDecimal.valueOf(conversionRate.getRate());
        return BigDecimal.valueOf(conversionRate.getProviderRateMap().get(provider));

    }

    protected String toProviderCode(ConversionRate conversionRate, String provider) {
        if(provider == null || provider.isEmpty() || conversionRate.getProviderRateMap().get(provider) == null)
            return null;
        return provider;
    }
}
