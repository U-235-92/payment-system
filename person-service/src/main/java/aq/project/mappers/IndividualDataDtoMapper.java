package aq.project.mappers;

import aq.project.dto.*;
import aq.project.entities.Address;
import aq.project.entities.Country;
import aq.project.entities.Individual;
import aq.project.entities.Person;
import aq.project.exceptions.UserNotExistsException;
import aq.project.repositories.PersonRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class IndividualDataDtoMapper {

    @Autowired
    private PersonRepository personRepository;

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "individual", expression = "java(toIndividual(dto))")
    @Mapping(target = "address", expression = "java(toAddress(dto.getAddress()))")
    @Mapping(target = "keycloakId", source = "keycloakUserId")
    @Mapping(target = "active", source = "active")
    public abstract Person toPerson(CreateIndividualDataDTO dto);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passportNumber", source = "passportNumber")
    protected abstract Individual toIndividual(CreateIndividualDataDTO dto);

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "individual", expression = "java(toIndividual(dto))")
    @Mapping(target = "address", expression = "java(toAddress(dto.getAddress()))")
    @Mapping(target = "keycloakId", source = "keycloakUserId")
    @Mapping(target = "active", source = "active")
    public abstract Person toPerson(UpdateIndividualDataDTO dto) throws UserNotExistsException;

    @Mapping(target = "email", expression = "java(getPersonEmail(dto.getKeycloakUserId()))")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "passportNumber", source = "passportNumber")
    protected abstract Individual toIndividual(UpdateIndividualDataDTO dto) throws UserNotExistsException;

    @Named("getPersonEmail")
    protected String getPersonEmail(String keycloakUserId) throws UserNotExistsException {
        String msg = String.format("Error occurred during update user. User with keycloakId %s does not exist", keycloakUserId);
        return personRepository.findEmailByKeycloakId(keycloakUserId).orElseThrow(() -> new UserNotExistsException(msg));
    }

    @Mapping(target = "state", source = "state")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "zip", source = "zipCode")
    @Mapping(target = "country", expression = "java(toCountry(address.getCountry()))")
    protected abstract Address toAddress(AddressDTO address);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    protected abstract Country toCountry(CountryDTO country);

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", expression = "java(person.getIndividual().getEmail())")
    @Mapping(target = "phoneNumber", expression = "java(person.getIndividual().getPhoneNumber())")
    @Mapping(target = "passportNumber", expression = "java(person.getIndividual().getPassportNumber())")
    @Mapping(target = "address", expression = "java(toAddressDTO(person.getAddress()))")
    public abstract IndividualDataResponseDTO toIndividualResponseDTO(Person person);

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
