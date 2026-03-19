package aq.project.util.entity;

import aq.project.dto.AddressDTO;
import aq.project.dto.CountryDTO;
import aq.project.dto.CreateIndividualDataEvent;
import aq.project.dto.UndoOperationDTO;

import java.util.UUID;

public abstract class DTO {

    public static CreateIndividualDataEvent getValidCreateIndividualDataEvent() {
        CreateIndividualDataEvent createIndividualDataEvent = new CreateIndividualDataEvent();
        createIndividualDataEvent.setKeycloakUserId(Constants.CORRECT_PERSON_KEYCLOAK_ID);
        createIndividualDataEvent.setFirstName(Constants.CORRECT_FIRST_NAME);
        createIndividualDataEvent.setLastName(Constants.CORRECT_LAST_NAME);
        createIndividualDataEvent.setEmail(Constants.CORRECT_EMAIL);
        createIndividualDataEvent.setPhoneNumber(Constants.CORRECT_PHONE);
        createIndividualDataEvent.setPassportNumber(Constants.CORRECT_PASSPORT);
        createIndividualDataEvent.setAddress(getValidAddressDTO(getValidCountryDTO()));
        return createIndividualDataEvent;
    }

    public static AddressDTO getValidAddressDTO(CountryDTO countryDTO) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState(Constants.CORRECT_STATE);
        addressDTO.setCity(Constants.CORRECT_CITY);
        addressDTO.setAddress(Constants.CORRECT_ADDRESS);
        addressDTO.setZipCode(Constants.CORRECT_ZIP);
        return addressDTO;
    }

    public static CountryDTO getValidCountryDTO() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName(Constants.COUNTRY_NAME);
        countryDTO.setCode(Constants.COUNTRY_CODE);
        return countryDTO;
    }

    public static UndoOperationDTO getValidUndoDeleteOperationDTO() {
        UndoOperationDTO undoOperationDTO = new UndoOperationDTO();
        undoOperationDTO.id(UUID.randomUUID().toString());
        undoOperationDTO.setOperation(UndoOperationDTO.OperationEnum.UNDO_DELETE_PERSON);
        undoOperationDTO.getPayload().put("person-keycloak-id", Constants.CORRECT_PERSON_KEYCLOAK_ID);
        undoOperationDTO.getPayload().put("timestamp", System.currentTimeMillis() + "");
        undoOperationDTO.getPayload().put("description", "JUnit test undo delete person");
        return undoOperationDTO;
    }
}
