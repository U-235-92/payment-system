package aq.project.mappers.dto;

import aq.project.dto.CurrencyResponse;
import aq.project.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class CurrencyResponseMapper {

    @Mapping(target = "code", expression = "java(toCode(currency))")
    @Mapping(target = "isoCode", expression = "java(toIsoCode(currency))")
    @Mapping(target = "description", expression = "java(toDescription(currency))")
    @Mapping(target = "active", expression = "java(toActive(currency))")
    @Mapping(target = "symbol", expression = "java(toSymbol(currency))")
    public abstract CurrencyResponse toCurrencyResponse(Currency currency);

    protected String toCode(Currency currency) {
        return currency.getIsoCode();
    }

    protected Integer toIsoCode(Currency currency) {
        return currency.getIsoNumeric();
    }

    protected String toDescription(Currency currency) {
        return currency.getDescription();
    }

    protected boolean toActive(Currency currency) {
        return currency.isActive();
    }

    protected String toSymbol(Currency currency) {
        return currency.getSymbol();
    }
}
