package aq.project.utils.mappers;

import aq.project.dto.PersonServiceCountryDto;
import aq.project.entities.Country;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UpdateCountryMapper {

    UpdateCountryMapper INSTANCE = Mappers.getMapper(UpdateCountryMapper.class);

    @Mapping(target = "instantEmbeddedData", ignore = true)
    void updateCountryFromDto(
            PersonServiceCountryDto dto,
            @MappingTarget Country country
    );
}
