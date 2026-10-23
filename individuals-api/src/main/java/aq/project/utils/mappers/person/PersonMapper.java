package aq.project.utils.mappers.person;

import aq.project.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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
    @Mapping(target = "country", expression = "java(toPersonServiceCountryDto(dto))")
    @Mapping(target = "address", expression = "java(toPersonServiceAddressDto(dto))")
    PersonServiceCreatePersonRequestDto toPersonServiceCreatePersonRequestDto(
            IndividualsApiServiceCreatePersonRequestDto dto);

    @Mapping(target = "name", source = "countryName")
    @Mapping(target = "code", source = "countryCode")
    PersonServiceCountryDto toPersonServiceCountryDto(
            IndividualsApiServiceCreatePersonRequestDto dto);

    @Mapping(target = "state", source = "state")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "zipCode", source = "zipCode")
    PersonServiceAddressDto toPersonServiceAddressDto(
            IndividualsApiServiceCreatePersonRequestDto dto);

    @Mapping(target = "personKeycloakId", source = "personKeycloakId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passportNumber", source = "passportNumber")
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "country", expression = "java(toPersonServiceCountryDto(dto))")
    @Mapping(target = "address", expression = "java(toPersonServiceAddressDto(dto))")
    PersonServiceUpdatePersonRequestDto toPersonServiceUpdatePersonRequestDto(
            IndividualsApiServiceUpdatePersonRequestDto dto);

    @Mapping(target = "name", source = "countryName")
    @Mapping(target = "code", source = "countryCode")
    PersonServiceCountryDto toPersonServiceCountryDto(
            IndividualsApiServiceUpdatePersonRequestDto dto);

    @Mapping(target = "state", source = "state")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "zipCode", source = "zipCode")
    PersonServiceAddressDto toPersonServiceAddressDto(
            IndividualsApiServiceUpdatePersonRequestDto dto);

    @Mapping(target = "personKeycloakId", ignore = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passportNumber", source = "passportNumber")
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "countryName", expression = "java(toCountryName(personServicePersonInfoResponseDto.getCountry()))")
    @Mapping(target = "countryCode", expression = "java(toCountryCode(personServicePersonInfoResponseDto.getCountry()))")
    @Mapping(target = "state", expression = "java(toState(personServicePersonInfoResponseDto.getAddress()))")
    @Mapping(target = "city", expression = "java(toCity(personServicePersonInfoResponseDto.getAddress()))")
    @Mapping(target = "address", expression = "java(toAddress(personServicePersonInfoResponseDto.getAddress()))")
    @Mapping(target = "zipCode", expression = "java(toZipCode(personServicePersonInfoResponseDto.getAddress()))")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toIndividualsApiServicePersonInfoResponseDto(
            @MappingTarget IndividualsApiServicePersonInfoResponseDto individualsApiServicePersonInfoResponseDto,
            PersonServicePersonInfoResponseDto personServicePersonInfoResponseDto);

    default String toCountryName(PersonServiceCountryDto dto) {
        return dto.getName();
    }

    default String toCountryCode(PersonServiceCountryDto dto) {
        return dto.getCode();
    }

    default String toState(PersonServiceAddressDto dto) {
        return dto.getState();
    }

    default String toCity(PersonServiceAddressDto dto) {
        return dto.getCity();
    }

    default String toAddress(PersonServiceAddressDto dto) {
        return dto.getAddress();
    }

    default String toZipCode(PersonServiceAddressDto dto) {
        return dto.getZipCode();
    }
}
