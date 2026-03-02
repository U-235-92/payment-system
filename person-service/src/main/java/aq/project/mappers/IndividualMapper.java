package aq.project.mappers;

import aq.project.dto.AddressDTO;
import aq.project.dto.CountryDTO;
import aq.project.dto.IndividualRequest;
import aq.project.dto.IndividualResponse;
import aq.project.entities.Address;
import aq.project.entities.Country;
import aq.project.entities.Individual;
import aq.project.entities.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class IndividualMapper {

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "individual", expression = "java(toIndividual(request))")
    @Mapping(target = "address", expression = "java(toAddress(request.getAddress()))")
    public abstract Person toPerson(IndividualRequest request);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passportNumber", source = "passportNumber")
    protected abstract Individual toIndividual(IndividualRequest request);

    @Mapping(target = "state", source = "state")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "zip", source = "zipCode")
    @Mapping(target = "country", expression = "java(toCountry(address.getCountry()))")
    protected abstract Address toAddress(AddressDTO address);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    protected abstract Country toCountry(CountryDTO country);

    @Mapping(target = "id", expression = "java(toStringUUID(person.getId()))")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", expression = "java(person.getIndividual().getEmail())")
    @Mapping(target = "phoneNumber", expression = "java(person.getIndividual().getPhoneNumber())")
    @Mapping(target = "passportNumber", expression = "java(person.getIndividual().getPassportNumber())")
    @Mapping(target = "address", expression = "java(toAddressDTO(person.getAddress()))")
    public abstract IndividualResponse toIndividualResponse(Person person);

    @Named("toStringUUID")
    protected String toStringUUID(UUID uuid) {
        return uuid.toString();
    }

    @Mapping(target = "country", expression = "java(toCountryDTO(address.getCountry()))")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "zipCode", source = "zip")
    protected abstract AddressDTO toAddressDTO(Address address);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    protected abstract CountryDTO toCountryDTO(Country country);
}
