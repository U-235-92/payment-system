package aq.project.util;

import aq.project.dto.*;

public abstract class TestDtoRepository {

    public static CountryDto getValidCountryDTO() {
        CountryDto countryDTO = new CountryDto();
        countryDTO.setName("Russia");
        countryDTO.setCode("RU");
        return countryDTO;
    }

    public static AddressDto getValidAddressDTO(CountryDto countryDTO) {
        AddressDto addressDTO = new AddressDto();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("updated_state");
        addressDTO.setCity("updated_city");
        addressDTO.setAddress("updated_address");
        addressDTO.setZipCode("updated_zipcode");
        return addressDTO;
    }

    public static UpdateIndividualDataDto getValidUpdateIndividualDataDTO(AddressDto addressDTO, String actualUserKeycloakId) {
        UpdateIndividualDataDto updateIndividualDataDTO = new UpdateIndividualDataDto();
        updateIndividualDataDTO.setKeycloakUserId(actualUserKeycloakId);
        updateIndividualDataDTO.setFirstName("updatedFirstName");
        updateIndividualDataDTO.setLastName("updatedLastName");
        updateIndividualDataDTO.phoneNumber("9876543210");
        updateIndividualDataDTO.setPassportNumber("9876543210");
        updateIndividualDataDTO.setAddress(addressDTO);
        return updateIndividualDataDTO;
    }

    public static UpdateIndividualDataDto getValidUpdateIndividualDataDtoWithUnknownKeycloakUserId(AddressDto addressDTO, String unknownUserKeycloakId) {
        UpdateIndividualDataDto updateIndividualDataDTO = new UpdateIndividualDataDto();
        updateIndividualDataDTO.setKeycloakUserId(unknownUserKeycloakId);
        updateIndividualDataDTO.setFirstName("updatedFirstName");
        updateIndividualDataDTO.setLastName("updatedLastName");
        updateIndividualDataDTO.phoneNumber("9876543210");
        updateIndividualDataDTO.setPassportNumber("9876543210");
        updateIndividualDataDTO.setAddress(addressDTO);
        return updateIndividualDataDTO;
    }

    public static UpdateUserDto getValidUpdateUserDTO(UpdateIndividualDataDto updateIndividualDataDTO, String actualUserKeycloakId) {
        return new UpdateUserDto()
                .keycloakUserId(actualUserKeycloakId)
                .password("password")
                .confirmPassword("password")
                .individualData(updateIndividualDataDTO);
    }

    public static UpdateUserDto getValidUpdateUserDtoWithUnknownKeycloakUserId(UpdateIndividualDataDto updateIndividualDataDTO, String unknownUserKeycloakId) {
        return new UpdateUserDto()
                .keycloakUserId(unknownUserKeycloakId)
                .password("password")
                .confirmPassword("password")
                .individualData(updateIndividualDataDTO);
    }

    public static UpdateUserDto getInvalidUpdateUserDtoWithNoMatchPassword(UpdateIndividualDataDto updateIndividualDataDTO, String actualUserKeycloakId) {
        return new UpdateUserDto()
                .keycloakUserId(actualUserKeycloakId)
                .password("foo")
                .confirmPassword("bar")
                .individualData(updateIndividualDataDTO);
    }

    public static UpdateUserDto getInvalidUpdateUserDtoWithNullFields(UpdateIndividualDataDto updateIndividualDataDTO, String actualUserKeycloakId) {
        return new UpdateUserDto()
                .keycloakUserId(actualUserKeycloakId)
                .password(null)
                .confirmPassword("bar")
                .individualData(updateIndividualDataDTO);
    }

    public static LoginUserDto getLoginUserDTO(String email, String password) {
        return new LoginUserDto().email(email).password(password);
    }

    public static CreateUserDto getValidCreateUserDTO() {
        CountryDto countryDTO = new CountryDto();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDto addressDTO = new AddressDto();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataDto createIndividualDataDTO = new CreateIndividualDataDto();
        createIndividualDataDTO.setFirstName("firstName");
        createIndividualDataDTO.setLastName("lastName");
        createIndividualDataDTO.setEmail("email@post.aq");
        createIndividualDataDTO.phoneNumber("1234567890");
        createIndividualDataDTO.setPassportNumber("1234567890");
        createIndividualDataDTO.setAddress(addressDTO);

        return new CreateUserDto()
                .username("username")
                .password("password")
                .confirmPassword("password")
                .individualData(createIndividualDataDTO);
    }

    public static CreateUserDto getDuplicateCreateUserDTO() {
        CountryDto countryDTO = new CountryDto();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDto addressDTO = new AddressDto();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataDto createIndividualDataDTO = new CreateIndividualDataDto();
        createIndividualDataDTO.setFirstName("Alice");
        createIndividualDataDTO.setLastName("K");
        createIndividualDataDTO.setEmail("alice@post.aq");
        createIndividualDataDTO.phoneNumber("1234567890");
        createIndividualDataDTO.setPassportNumber("1234567890");
        createIndividualDataDTO.setAddress(addressDTO);

        return new CreateUserDto()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad407")
                .username("alice")
                .password("password")
                .confirmPassword("password")
                .individualData(createIndividualDataDTO);
    }

    public static CreateUserDto getIncorrectCreateUserDTOWithDoNotMatchPasswords() {
        CountryDto countryDTO = new CountryDto();
        countryDTO.setName("country");
        countryDTO.setCode("cty");

        AddressDto addressDTO = new AddressDto();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("state");
        addressDTO.setCity("city");
        addressDTO.setAddress("address");
        addressDTO.setZipCode("zipcode");

        CreateIndividualDataDto individualDataEvent = new CreateIndividualDataDto();
        individualDataEvent.setFirstName("Bob");
        individualDataEvent.setLastName("K");
        individualDataEvent.setEmail("bob@post.aq");
        individualDataEvent.phoneNumber("1234567890");
        individualDataEvent.setPassportNumber("1234567890");
        individualDataEvent.setAddress(addressDTO);

        return new CreateUserDto()
                .keycloakUserId("c0391ed2-80b5-400c-8fd2-4d374acad477")
                .username("bob")
                .password("password")
                .confirmPassword("123")
                .individualData(individualDataEvent);
    }

    public static CreateUserDto getIncorrectCreateUserDTOWithNullFields() {
        return new CreateUserDto()
                .username(null)
                .password("password")
                .confirmPassword("123");
    }

    public static CreateUserDto getIncorrectCreateUserDTOWithNullIndividualData() {
        return new CreateUserDto()
                .username("test")
                .password("password")
                .confirmPassword("123")
                .individualData(null);
    }
}
