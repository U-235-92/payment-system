package aq.project._utils.entities;

import aq.project.dto.*;
import aq.project.entities.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PersonServiceEntities {

    public static PersonServiceCreatePersonRequestDto getValidPersonServiceCreatePersonRequestDto() {
        PersonServiceCreatePersonRequestDto dto = new PersonServiceCreatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setFirstName("FirstName");
        dto.setLastName("LastName");
        dto.setEmail("person@post.aq");
        dto.setPhoneNumber("123456789");
        dto.setPassportNumber("123456789");
        dto.setActive(true);
        dto.setAddress(getValidPersonServiceAddressDto());
        dto.setCountry(getValidPersonServiceCountryDto());
        return dto;
    }

    public static PersonServiceAddressDto getValidPersonServiceAddressDto() {
        PersonServiceAddressDto dto = new PersonServiceAddressDto();
        dto.setAddress("Address");
        dto.setCity("City");
        dto.setState("State");
        dto.setZipCode("ZipCode");
        return dto;
    }

    public static PersonServiceCountryDto getValidPersonServiceCountryDto() {
        PersonServiceCountryDto dto = new PersonServiceCountryDto();
        dto.setCode("ATA");
        dto.setName("Antarctica");
        return dto;
    }

    public static PersonServiceCreatePersonRequestDto getInvalidPersonServiceCreatePersonRequestDto() {
        PersonServiceCreatePersonRequestDto dto = new PersonServiceCreatePersonRequestDto();
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

    public static PersonServiceUpdatePersonRequestDto getValidPersonServiceUpdatePersonRequestDto() {
        PersonServiceUpdatePersonRequestDto dto = new PersonServiceUpdatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setCountry(getValidPersonServiceCountryDto());
        dto.setAddress(getValidPersonServiceAddressDto());
        dto.setFirstName("FirstName");
        dto.setLastName("LastName");
        dto.setPhoneNumber("123456789");
        dto.setPassportNumber("123456789");
        dto.setActive(true);
        return dto;
    }

    public static PersonServiceUpdatePersonRequestDto getInvalidPersonServiceUpdatePersonRequestDto() {
        PersonServiceUpdatePersonRequestDto dto = new PersonServiceUpdatePersonRequestDto();
        dto.setPersonKeycloakId(null);
        dto.setFirstName(null);
        dto.setLastName(null);
        dto.setPhoneNumber("123456789");
        dto.setPassportNumber("123456789");
        dto.setActive(true);
        dto.setAddress(null);
        return dto;
    }

    public static PersonServiceUndoDeletePersonRequestDto getValidPersonServiceUndoDeletePersonRequestDto() {
        PersonServiceUndoDeletePersonRequestDto dto = new PersonServiceUndoDeletePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setDescription("Description");
        return dto;
    }

    public static PersonServiceUndoDeletePersonRequestDto getInvalidPersonServiceUndoDeletePersonRequestDto() {
        PersonServiceUndoDeletePersonRequestDto dto = new PersonServiceUndoDeletePersonRequestDto();
        dto.setPersonKeycloakId(null);
        dto.setDescription("Description");
        return dto;
    }

    public static PersonServiceUndoUpdatePersonRequestDto getValidPersonServiceUndoUpdatePersonRequestDto() {
        PersonServiceUndoUpdatePersonRequestDto dto = new PersonServiceUndoUpdatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setDescription("Description");
        return dto;
    }

    public static PersonServiceUndoUpdatePersonRequestDto getInvalidPersonServiceUndoUpdatePersonRequestDto() {
        PersonServiceUndoUpdatePersonRequestDto dto = new PersonServiceUndoUpdatePersonRequestDto();
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