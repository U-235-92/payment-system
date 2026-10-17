package aq.project.utils.mappers.person;

import aq.project.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PersonMapper {

    PersonMapper INSTANCE = Mappers.getMapper(PersonMapper.class);

    @Mapping(target = "personKeycloakId", source = "personKeycloakId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passportNumber", source = "passportNumber")
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "address", expression = "java(toPersonServiceAddressDto(individualsApiServiceCreatePersonRequestDto))")
    PersonServiceCreatePersonRequestDto toPersonServiceCreatePersonRequestDto(
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto);

    @Mapping(target = "state", source = "state")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "country", expression = "java(toPersonServiceCountryDto(individualsApiServiceCreatePersonRequestDto))")
    PersonServiceAddressDto toPersonServiceAddressDto(
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto);

    @Mapping(target = "name", source = "countryName")
    @Mapping(target = "code", source = "countryCode")
    PersonServiceCountryDto toPersonServiceCountryDto(
            IndividualsApiServiceCreatePersonRequestDto individualsApiServiceCreatePersonRequestDto);

    @Mapping(target = "personKeycloakId", source = "personKeycloakId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passportNumber", source = "passportNumber")
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "address", expression = "java(toPersonServiceAddressDto(individualsApiServiceUpdatePersonRequestDto))")
    PersonServiceUpdatePersonRequestDto toPersonServiceUpdatePersonRequestDto(
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto);

    @Mapping(target = "state", source = "state")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "country", expression = "java(toPersonServiceCountryDto(individualsApiServiceUpdatePersonRequestDto))")
    PersonServiceAddressDto toPersonServiceAddressDto(
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto);

    @Mapping(target = "name", source = "countryName")
    @Mapping(target = "code", source = "countryCode")
    PersonServiceCountryDto toPersonServiceCountryDto(
            IndividualsApiServiceUpdatePersonRequestDto individualsApiServiceUpdatePersonRequestDto);
}
