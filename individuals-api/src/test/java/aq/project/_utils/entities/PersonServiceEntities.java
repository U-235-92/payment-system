package aq.project._utils.entities;

import aq.project.dto.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PersonServiceEntities {

    public static PersonServicePersonInfoResponseDto getValdPersonServicePersonInfoResponseDto() {
        PersonServicePersonInfoResponseDto dto = new PersonServicePersonInfoResponseDto();
        dto.setFirstName("Firstname");
        dto.setLastName("Lastname");
        dto.setEmail(getValidIndividualsApiServiceLoginPersonRequestDto().getEmail());
        dto.setPhoneNumber("85858585858");
        dto.setPassportNumber(UUID.randomUUID().toString());
        dto.setCountry(getValidPersonServiceCountryDto());
        dto.setAddress(getValidPersonServiceAddressDto());
        return dto;
    }

    private static PersonServiceAddressDto getValidPersonServiceAddressDto() {
        PersonServiceAddressDto dto = new PersonServiceAddressDto();
        dto.setAddress("Address");
        dto.setCity("City");
        dto.setState("State");
        dto.setZipCode("ZipCode");
        return dto;
    }

    private static PersonServiceCountryDto getValidPersonServiceCountryDto() {
        PersonServiceCountryDto dto = new PersonServiceCountryDto();
        dto.setCode("ATA");
        dto.setName("Antarctica");
        return dto;
    }

    public static IndividualsApiServiceLoginPersonRequestDto getValidIndividualsApiServiceLoginPersonRequestDto() {
        IndividualsApiServiceLoginPersonRequestDto dto = new IndividualsApiServiceLoginPersonRequestDto();
        dto.setEmail("test@mail.aq"); // See realm-config.json by: [Ctrl + F "username": "test"]
        dto.setPassword("123"); // See realm-config.json by: [Ctrl + F "username": "test"]
        return dto;
    }

    public static IndividualsApiServiceLoginPersonRequestDto getInvalidIndividualsApiServiceLoginPersonRequestDto() {
        IndividualsApiServiceLoginPersonRequestDto dto = new IndividualsApiServiceLoginPersonRequestDto();
        dto.setEmail(null);
        dto.setPassword("123");
        return dto;
    }

    public static IndividualsApiServiceCreatePersonRequestDto getValidIndividualsApiServiceCreatePersonRequestDto() {
        IndividualsApiServiceCreatePersonRequestDto dto = new IndividualsApiServiceCreatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setUsername("person");
        dto.setPassword("password");
        dto.setConfirmPassword("password");
        dto.setFirstName("Firstname");
        dto.setLastName("Lastname");
        dto.setEmail("person@post.aq");
        dto.setPhoneNumber("85858585858");
        dto.setPassportNumber(UUID.randomUUID().toString());
        dto.setCountryName("Antarctica");
        dto.setCountryCode("ATA");
        dto.setState("state");
        dto.setCity("City");
        dto.setAddress("address");
        dto.setZipCode("zipCode");
        dto.setActive(true);
        return dto;
    }

    public static IndividualsApiServiceCreatePersonRequestDto getInvalidIndividualsApiServiceCreatePersonRequestDto() {
        IndividualsApiServiceCreatePersonRequestDto dto = new IndividualsApiServiceCreatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setUsername(null);
        dto.setPassword("password");
        dto.setConfirmPassword("password");
        dto.setFirstName(null);
        dto.setLastName("LastName");
        dto.setEmail("person@post.aq");
        dto.setPhoneNumber("85858585858");
        dto.setPassportNumber(UUID.randomUUID().toString());
        dto.setCountryName("Antarctica");
        dto.setCountryCode("ATA");
        dto.setState("state");
        dto.setCity("City");
        dto.setAddress("address");
        dto.setZipCode("zipCode");
        dto.setActive(true);
        return dto;
    }

    public static IndividualsApiServiceUpdatePersonRequestDto getValidIndividualsApiServiceUpdatePersonRequestDto() {
        IndividualsApiServiceUpdatePersonRequestDto dto = new IndividualsApiServiceUpdatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setPassword("password");
        dto.setConfirmPassword("password");
        dto.setFirstName("Firstname");
        dto.setLastName("Lastname");
        dto.setPhoneNumber("85858585858");
        dto.setPassportNumber(UUID.randomUUID().toString());
        dto.setCountryName("Antarctica");
        dto.setCountryCode("ATA");
        dto.setState("state");
        dto.setCity("City");
        dto.setAddress("address");
        dto.setZipCode("zipCode");
        dto.setActive(true);
        return dto;
    }

    public static IndividualsApiServiceUpdatePersonRequestDto getInvalidIndividualsApiServiceUpdatePersonRequestDto() {
        IndividualsApiServiceUpdatePersonRequestDto dto = new IndividualsApiServiceUpdatePersonRequestDto();
        dto.setPersonKeycloakId(UUID.randomUUID());
        dto.setPassword("drowssap");
        dto.setConfirmPassword("password");
        dto.setFirstName(null);
        dto.setLastName(null);
        dto.setPhoneNumber("0123456789");
        dto.setPassportNumber(UUID.randomUUID().toString());
        dto.setCountryName("Antarctica");
        dto.setCountryCode("ATA");
        dto.setState("state");
        dto.setCity("city");
        dto.setAddress("address");
        dto.setZipCode("zipCode");
        dto.setActive(true);
        return dto;
    }
}
