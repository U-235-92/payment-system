package aq.project.util;

import aq.project.dto.*;

public abstract class TestDtoRepository {

    public static CountryDTO getValidCountryDTO() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("Russia");
        countryDTO.setCode("RU");
        return countryDTO;
    }

    public static AddressDTO getValidAddressDTO(CountryDTO countryDTO) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("updated_state");
        addressDTO.setCity("updated_city");
        addressDTO.setAddress("updated_address");
        addressDTO.setZipCode("updated_zipcode");
        return addressDTO;
    }

    public static UpdateIndividualDataDTO getValidUpdateIndividualDataDTO(AddressDTO addressDTO, String actualUserKeycloakId) {
        UpdateIndividualDataDTO updateIndividualDataDTO = new UpdateIndividualDataDTO();
        updateIndividualDataDTO.setKeycloakUserId(actualUserKeycloakId);
        updateIndividualDataDTO.setFirstName("updatedFirstName");
        updateIndividualDataDTO.setLastName("updatedLastName");
        updateIndividualDataDTO.phoneNumber("9876543210");
        updateIndividualDataDTO.setPassportNumber("9876543210");
        updateIndividualDataDTO.setAddress(addressDTO);
        return updateIndividualDataDTO;
    }

    public static UpdateIndividualDataDTO getValidUpdateIndividualDataDtoWithUnknownKeycloakUserId(AddressDTO addressDTO, String unknownUserKeycloakId) {
        UpdateIndividualDataDTO updateIndividualDataDTO = new UpdateIndividualDataDTO();
        updateIndividualDataDTO.setKeycloakUserId(unknownUserKeycloakId);
        updateIndividualDataDTO.setFirstName("updatedFirstName");
        updateIndividualDataDTO.setLastName("updatedLastName");
        updateIndividualDataDTO.phoneNumber("9876543210");
        updateIndividualDataDTO.setPassportNumber("9876543210");
        updateIndividualDataDTO.setAddress(addressDTO);
        return updateIndividualDataDTO;
    }

    public static UpdateUserDTO getValidUpdateUserDTO(UpdateIndividualDataDTO updateIndividualDataDTO, String actualUserKeycloakId) {
        return new UpdateUserDTO()
                .keycloakUserId(actualUserKeycloakId)
                .password("password")
                .confirmPassword("password")
                .individualData(updateIndividualDataDTO);
    }

    public static UpdateUserDTO getValidUpdateUserDtoWithUnknownKeycloakUserId(UpdateIndividualDataDTO updateIndividualDataDTO, String unknownUserKeycloakId) {
        return new UpdateUserDTO()
                .keycloakUserId(unknownUserKeycloakId)
                .password("password")
                .confirmPassword("password")
                .individualData(updateIndividualDataDTO);
    }

    public static UpdateUserDTO getInvalidUpdateUserDtoWithNoMatchPassword(UpdateIndividualDataDTO updateIndividualDataDTO, String actualUserKeycloakId) {
        return new UpdateUserDTO()
                .keycloakUserId(actualUserKeycloakId)
                .password("foo")
                .confirmPassword("bar")
                .individualData(updateIndividualDataDTO);
    }

    public static UpdateUserDTO getInvalidUpdateUserDtoWithNullFields(UpdateIndividualDataDTO updateIndividualDataDTO, String actualUserKeycloakId) {
        return new UpdateUserDTO()
                .keycloakUserId(actualUserKeycloakId)
                .password(null)
                .confirmPassword("bar")
                .individualData(updateIndividualDataDTO);
    }

    public static LoginUserDTO getLoginUserDTO(String email, String password) {
        return new LoginUserDTO().email(email).password(password);
    }

    public static CreateUserDTO getValidCreateUserDTO() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataDTO createIndividualDataDTO = new CreateIndividualDataDTO();
        createIndividualDataDTO.setFirstName("firstName");
        createIndividualDataDTO.setLastName("lastName");
        createIndividualDataDTO.setEmail("email@post.aq");
        createIndividualDataDTO.phoneNumber("1234567890");
        createIndividualDataDTO.setPassportNumber("1234567890");
        createIndividualDataDTO.setAddress(addressDTO);

        return new CreateUserDTO()
                .username("username")
                .password("password")
                .confirmPassword("password")
                .individualData(createIndividualDataDTO);
    }

    public static CreateUserDTO getDuplicateCreateUserDTO() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataDTO createIndividualDataDTO = new CreateIndividualDataDTO();
        createIndividualDataDTO.setFirstName("Alice");
        createIndividualDataDTO.setLastName("K");
        createIndividualDataDTO.setEmail("alice@post.aq");
        createIndividualDataDTO.phoneNumber("1234567890");
        createIndividualDataDTO.setPassportNumber("1234567890");
        createIndividualDataDTO.setAddress(addressDTO);

        return new CreateUserDTO()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad407")
                .username("alice")
                .password("password")
                .confirmPassword("password")
                .individualData(createIndividualDataDTO);
    }

    public static CreateUserDTO getIncorrectCreateUserDTOWithDoNotMatchPasswords() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataDTO individualDataEvent = new CreateIndividualDataDTO();
        individualDataEvent.setFirstName("Bob");
        individualDataEvent.setLastName("K");
        individualDataEvent.setEmail("bob@post.aq");
        individualDataEvent.phoneNumber("1234567890");
        individualDataEvent.setPassportNumber("1234567890");
        individualDataEvent.setAddress(addressDTO);

        return new CreateUserDTO()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad477")
                .username("bob")
                .password("password")
                .confirmPassword("123")
                .individualData(individualDataEvent);
    }

    public static CreateUserDTO getIncorrectCreateUserDTOWithNullFields() {
        return new CreateUserDTO()
                .username(null)
                .password("password")
                .confirmPassword("123");
    }

    public static CreateUserDTO getIncorrectCreateUserDTOWithNullIndividualData() {
        return new CreateUserDTO()
                .username("test")
                .password("password")
                .confirmPassword("123")
                .individualData(null);
    }
}
