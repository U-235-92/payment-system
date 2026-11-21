package aq.project._utils.entities;

import aq.project.dto.*;
import aq.project.entities.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PersonServiceEntities {

    public static CreatePersonRequestDto getValidCreatePersonRequestDto() {
        CreatePersonRequestDto dto = new CreatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setFirstName("FirstName");
        dto.setLastName("LastName");
        dto.setEmail("person@post.aq");
        dto.setPhoneNumber("123456789");
        dto.setPassportNumber("123456789");
        dto.setActive(true);
        dto.setAddress(getValidAddressDto());
        dto.setCountry(getValidCountryDto());
        return dto;
    }

    public static AddressDto getValidAddressDto() {
        AddressDto dto = new AddressDto();
        dto.setAddress("Address");
        dto.setCity("City");
        dto.setState("State");
        dto.setZipCode("ZipCode");
        return dto;
    }

    public static CountryDto getValidCountryDto() {
        CountryDto dto = new CountryDto();
        dto.setCode("ATA");
        dto.setName("Antarctica");
        return dto;
    }

    public static CreatePersonRequestDto getInvalidCreatePersonRequestDto() {
        CreatePersonRequestDto dto = new CreatePersonRequestDto();
        dto.setPersonKeycloakId(null);
        dto.setFirstName(null);
        dto.setLastName("LastName");
        dto.setEmail(null);
        dto.setPhoneNumber("123456789");
        dto.setPassportNumber("123456789");
        dto.setActive(true);
        dto.setAddress(null);
        return dto;
    }

    public static UpdatePersonRequestDto getValidUpdatePersonRequestDto() {
        UpdatePersonRequestDto dto = new UpdatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setCountry(getValidCountryDto());
        dto.setAddress(getValidAddressDto());
        dto.setFirstName("FirstName");
        dto.setLastName("LastName");
        dto.setPhoneNumber("123456789");
        dto.setPassportNumber("123456789");
        dto.setActive(true);
        return dto;
    }

    public static UpdatePersonRequestDto getInvalidUpdatePersonRequestDto() {
        UpdatePersonRequestDto dto = new UpdatePersonRequestDto();
        dto.setPersonKeycloakId(null);
        dto.setFirstName(null);
        dto.setLastName(null);
        dto.setPhoneNumber("123456789");
        dto.setPassportNumber("123456789");
        dto.setActive(true);
        dto.setAddress(null);
        return dto;
    }

    public static UndoDeletePersonRequestDto getValidUndoDeletePersonRequestDto() {
        UndoDeletePersonRequestDto dto = new UndoDeletePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setDescription("Description");
        return dto;
    }

    public static UndoDeletePersonRequestDto getInvalidUndoDeletePersonRequestDto() {
        UndoDeletePersonRequestDto dto = new UndoDeletePersonRequestDto();
        dto.setPersonKeycloakId(null);
        dto.setDescription("Description");
        return dto;
    }

    public static UndoUpdatePersonRequestDto getValidUndoUpdatePersonRequestDto() {
        UndoUpdatePersonRequestDto dto = new UndoUpdatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setDescription("Description");
        return dto;
    }

    public static UndoUpdatePersonRequestDto getInvalidUndoUpdatePersonRequestDto() {
        UndoUpdatePersonRequestDto dto = new UndoUpdatePersonRequestDto();
        dto.setPersonKeycloakId(null);
        dto.setDescription("Description");
        return dto;
    }

    public static Person getValidPerson() {
        Person person = new Person();
        person.setKeycloakId(UUID.randomUUID().toString());
        person.setAddress(getValidAddress());
        person.setIndividual(getValidIndividual());
        person.setFirstName("FirstName");
        person.setLastName("LastName");
        person.setActive(true);
        person.setInstantEmbeddedData(new InstantEmbeddedData());
        return person;
    }

    public static Person getInvalidPerson() {
        Person person = new Person();
        person.setKeycloakId(null);
        person.setAddress(getInvalidAddress());
        person.setIndividual(getInvalidIndividual());
        person.setFirstName(null);
        person.setLastName("LastName");
        person.setActive(true);
        person.setInstantEmbeddedData(new InstantEmbeddedData());
        return person;
    }
    
    public static Individual getValidIndividual() {
        Individual individual = new Individual();
        individual.setEmail("person@post.aq");
        individual.setPhoneNumber("123456789");
        individual.setPassportNumber("123456789");
        individual.setInstantEmbeddedData(new InstantEmbeddedData());
        return individual;
    }

    public static Individual getInvalidIndividual() {
        Individual individual = new Individual();
        individual.setEmail("");
        individual.setPhoneNumber(null);
        individual.setPassportNumber(null);
        individual.setInstantEmbeddedData(new InstantEmbeddedData());
        return individual;
    }
    
    public static Address getValidAddress() {
        Address address = new Address();
        address.setState("State");
        address.setCity("City");
        address.setAddress("Antarctica");
        address.setZip("ZipCode");
        address.setInstantEmbeddedData(new InstantEmbeddedData());
        return address;
    }

    public static Address getInvalidAddress() {
        Address address = new Address();
        address.setState("");
        address.setCity(null);
        address.setZip(null);
        address.setInstantEmbeddedData(new InstantEmbeddedData());
        return address;
    }

    public static Country getValidCountry() {
        Country country = new Country();
        country.setCode("ATA");
        country.setName("Antarctica");
        country.setInstantEmbeddedData(new InstantEmbeddedData());
        return country;
    }

    public static Country getInvalidCountry() {
        Country country = new Country();
        country.setCode("TEST");
        country.setName(null);
        return country;
    }
}