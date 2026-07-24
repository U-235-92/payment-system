package aq.project.utils.mappers.dto;

import aq.project.dto.RateProviderResponse;
import aq.project.entities.RateProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.OffsetDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class RateProviderResponseMapper {

    @Mapping(target = "providerCode", expression = "java(toProviderCode(rateProvider))")
    @Mapping(target = "providerName", expression = "java(toProviderName(rateProvider))")
    @Mapping(target = "description", expression = "java(toDescription(rateProvider))")
    @Mapping(target = "date", expression = "java(toRateProviderDate(rateProvider))")
    @Mapping(target = "active", expression = "java(toActive(rateProvider))")
    public abstract RateProviderResponse toRateProviderResponse(RateProvider rateProvider);

    protected String toProviderCode(RateProvider rateProvider) {
        return rateProvider.getCode();
    }

    protected String toProviderName(RateProvider rateProvider) {
        return rateProvider.getName();
    }

    protected String toDescription(RateProvider rateProvider) {
        return rateProvider.getDescription();
    }

    protected OffsetDateTime toRateProviderDate(RateProvider rateProvider) {
        return rateProvider.getModifiedAt();
    }

    protected boolean toActive(RateProvider rateProvider) {
        return rateProvider.isActive();
    }
}
