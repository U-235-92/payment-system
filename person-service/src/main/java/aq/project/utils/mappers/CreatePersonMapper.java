package aq.project.utils.mappers;

import aq.project.dto.*;
import aq.project.entities.Address;
import aq.project.entities.Country;
import aq.project.entities.Individual;
import aq.project.entities.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CreatePersonMapper {

    CreatePersonMapper INSTANCE = Mappers.getMapper(CreatePersonMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", source = "personKeycloakId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "country", expression = "java(toCountry(dto.getCountry()))")
    @Mapping(target = "address", expression = "java(toAddress(dto.getAddress()))")
    @Mapping(target = "individual", expression = "java(toIndividual(dto))")
    @Mapping(target = "instantEmbeddedData", ignore = true)
    Person toPerson(PersonServiceCreatePersonRequestDto dto);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "instantEmbeddedData", ignore = true)
    Country toCountry(PersonServiceCountryDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", source = "state")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "zip", source = "zipCode")
    @Mapping(target = "instantEmbeddedData", ignore = true)
    Address toAddress(PersonServiceAddressDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "passportNumber", source = "passportNumber")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "instantEmbeddedData", ignore = true)
    Individual toIndividual(PersonServiceCreatePersonRequestDto dto);

    PersonServicePersonInfoResponseDto toPersonServicePersonInfoResponseDto(Person person);
}
