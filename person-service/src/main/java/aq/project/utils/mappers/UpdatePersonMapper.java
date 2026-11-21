package aq.project.utils.mappers;

import aq.project.dto.AddressDto;
import aq.project.dto.UpdatePersonRequestDto;
import aq.project.entities.Address;
import aq.project.entities.Individual;
import aq.project.entities.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UpdatePersonMapper {

    UpdatePersonMapper INSTANCE = Mappers.getMapper(UpdatePersonMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "instantEmbeddedData", ignore = true)
    @Mapping(target = "country", ignore = true)
    void updatePersonFromDto(
            UpdatePersonRequestDto dto,
            @MappingTarget Person person
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instantEmbeddedData", ignore = true)
    void updateAddressFromDto(
            AddressDto dto,
            @MappingTarget Address address
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instantEmbeddedData", ignore = true)
    @Mapping(target = "email", ignore = true)
    void updateIndividualFromDto(
            UpdatePersonRequestDto dto,
            @MappingTarget Individual individual
    );
}
