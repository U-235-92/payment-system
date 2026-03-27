package aq.project.controllers;

import aq.project.dto.CreateIndividualDataDTO;
import aq.project.dto.IndividualDataResponseDTO;
import aq.project.dto.UndoOperationDTO;
import aq.project.dto.UpdateIndividualDataDTO;
import aq.project.entities.Person;
import aq.project.entities.UndoOperation;
import aq.project.exceptions.*;
import aq.project.mappers.IndividualDataDtoMapper;
import aq.project.mappers.UndoOperationDtoMapper;
import aq.project.services.PersonService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/person")
public class PersonRestController {

    private final PersonService personService;

    private final UndoOperationDtoMapper undoOperationDtoMapper;
    private final IndividualDataDtoMapper individualDataDtoMapper;

    @PostMapping("/create-person")
    @Timed(value = "person_service.create_person_time")
    public ResponseEntity<String> createPerson(@RequestBody CreateIndividualDataDTO createIndividualDataDTO) throws UserExistsException, CountryNotExistsException {
        Person person = individualDataDtoMapper.toPerson(createIndividualDataDTO);
        String userId = personService.createPerson(person);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(userId);
    }

    @DeleteMapping("/delete-person-by-keycloak-id/{keycloakId}")
    @Timed(value = "person_service.delete_person_by_keycloak_id_time")
    public ResponseEntity<Void> deletePersonByKeycloakId(@PathVariable String keycloakId) throws UserNotExistsException {
        personService.deletePersonByKeycloakId(keycloakId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/undo-delete-person-by-keycloak-id")
    @Timed(value = "person_service.undo_delete_person_by_keycloak_id_time")
    public ResponseEntity<Void> undoDeletePerson(@RequestBody UndoOperationDTO undoOperationDTO) throws IllegalUndoOperationPayloadPropertyException, NotFoundRevisionException, NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        UndoOperation undoOperation = undoOperationDtoMapper.toUndoOperation(undoOperationDTO);
        personService.undoDeletePerson(undoOperation);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update-person")
    @Timed(value = "person_service.update_person_time")
    public ResponseEntity<Void> updatePerson(@RequestBody UpdateIndividualDataDTO updateIndividualDataDTO) throws UserNotExistsException, CountryNotExistsException {
        Person person = individualDataDtoMapper.toPerson(updateIndividualDataDTO);
        personService.updatePerson(person);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/undo-update-person")
    @Timed(value = "person_service.undo_update_person_time")
    public ResponseEntity<Void> undoUpdatePerson(@RequestBody UndoOperationDTO undoOperationDTO) throws IllegalUndoOperationPayloadPropertyException, NotFoundRevisionException, NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        UndoOperation undoOperation = undoOperationDtoMapper.toUndoOperation(undoOperationDTO);
        personService.undoUpdatePerson(undoOperation);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-person-by-keycloak-id/{keycloakId}")
    @Timed(value = "person_service.get_person_by_keycloak_id_time")
    public ResponseEntity<IndividualDataResponseDTO> getPersonByKeycloakId(@PathVariable String keycloakId) throws UserNotExistsException {
        IndividualDataResponseDTO response = individualDataDtoMapper.toIndividualResponseDTO(personService.getPersonByKeycloakId(keycloakId));
        return ResponseEntity.ok().body(response);
    }
}