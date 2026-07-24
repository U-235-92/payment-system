package aq.project.util.entity;

import aq.project.dto.*;

public abstract class DTO {

    public static UpdateIndividualDataDto getUpdateIndividualDataDTO() {
        UpdateIndividualDataDto updateIndividualDataDTO = new UpdateIndividualDataDto();
        updateIndividualDataDTO.setKeycloakUserId(Constants.CORRECT_PERSON_KEYCLOAK_ID);
        updateIndividualDataDTO.setFirstName("Hello");
        updateIndividualDataDTO.setLastName("World");
        updateIndividualDataDTO.setPhoneNumber(Constants.CORRECT_PHONE);
        updateIndividualDataDTO.setPassportNumber(Constants.CORRECT_PASSPORT);
        updateIndividualDataDTO.setAddress(getValidAddressDTO(getValidCountryDTO()));
        return updateIndividualDataDTO;
    }

    public static CreateIndividualDataDto getValidCreateIndividualDataDTO() {
        CreateIndividualDataDto createIndividualDataEvent = new CreateIndividualDataDto();
        createIndividualDataEvent.setKeycloakUserId(Constants.CORRECT_PERSON_KEYCLOAK_ID);
        createIndividualDataEvent.setFirstName(Constants.CORRECT_FIRST_NAME);
        createIndividualDataEvent.setLastName(Constants.CORRECT_LAST_NAME);
        createIndividualDataEvent.setEmail(Constants.CORRECT_EMAIL);
        createIndividualDataEvent.setPhoneNumber(Constants.CORRECT_PHONE);
        createIndividualDataEvent.setPassportNumber(Constants.CORRECT_PASSPORT);
        createIndividualDataEvent.setAddress(getValidAddressDTO(getValidCountryDTO()));
        return createIndividualDataEvent;
    }

    public static AddressDto getValidAddressDTO(CountryDto countryDTO) {
        AddressDto addressDTO = new AddressDto();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState(Constants.CORRECT_STATE);
        addressDTO.setCity(Constants.CORRECT_CITY);
        addressDTO.setAddress(Constants.CORRECT_ADDRESS);
        addressDTO.setZipCode(Constants.CORRECT_ZIP);
        return addressDTO;
    }

    public static CountryDto getValidCountryDTO() {
        CountryDto countryDTO = new CountryDto();
        countryDTO.setName(Constants.COUNTRY_NAME);
        countryDTO.setCode(Constants.COUNTRY_CODE);
        return countryDTO;
    }

    public static UndoOperationDto getValidUndoDeleteOperationDTO() {
        UndoOperationDto undoOperationDTO = new UndoOperationDto();
        undoOperationDTO.setOperation(UndoOperationDto.OperationEnum.UNDO_DELETE_PERSON);
        undoOperationDTO.getPayload().put("person-keycloak-id", Constants.CORRECT_PERSON_KEYCLOAK_ID);
        undoOperationDTO.getPayload().put("timestamp", System.currentTimeMillis() + "");
        undoOperationDTO.getPayload().put("description", "JUnit test undo delete person");
        return undoOperationDTO;
    }

    public static UndoOperationDto getValidUndoUpdateOperationDTO() {
        UndoOperationDto undoOperationDTO = new UndoOperationDto();
        undoOperationDTO.setOperation(UndoOperationDto.OperationEnum.UNDO_UPDATE_PERSON);
        undoOperationDTO.getPayload().put("person-keycloak-id", Constants.CORRECT_PERSON_KEYCLOAK_ID);
        undoOperationDTO.getPayload().put("timestamp", System.currentTimeMillis() + "");
        undoOperationDTO.getPayload().put("description", "JUnit test undo update person");
        return undoOperationDTO;
    }
}
