package aq.project.mappers;

import aq.project.dto.AddressRequest;
import aq.project.dto.CountryRequest;
import aq.project.dto.IndividualUserRegistrationRequest;
import aq.project.entities.Address;
import aq.project.entities.Country;
import aq.project.entities.Individual;
import aq.project.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class IndividualUserRegistrationMapper {

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "individual", expression = "java(toIndividual(request))")
    @Mapping(target = "address", expression = "java(toAddress(request.getAddress()))")
    public abstract User toUser(IndividualUserRegistrationRequest request);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passportNumber", source = "passportNumber")
    public abstract Individual toIndividual(IndividualUserRegistrationRequest request);

    @Mapping(target = "state", source = "state")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "zip", source = "zipCode")
    @Mapping(target = "country", expression = "java(toCountry(address.getCountry()))")
    public abstract Address toAddress(AddressRequest address);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    public abstract Country toCountry(CountryRequest country);
}
