package aq.project.controllers;

import aq.project.dto.CreateIndividualDataEvent;
import aq.project.dto.IndividualDataResponse;
import aq.project.dto.UndoOperationDTO;
import aq.project.dto.UpdateIndividualDataEvent;
import aq.project.entities.Person;
import aq.project.entities.UndoEvent;
import aq.project.exceptions.*;
import aq.project.mappers.IndividualMapper;
import aq.project.mappers.UndoOperationMapper;
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

    private final IndividualMapper individualMapper;
    private final UndoOperationMapper undoOperationMapper;

    @PostMapping("/create-person")
    @Timed(value = "person_service.create_person_time")
    public ResponseEntity<String> createPerson(@RequestBody CreateIndividualDataEvent createIndividualDataEvent) throws UserExistsException, CountryNotExistsException {
        Person person = individualMapper.toPerson(createIndividualDataEvent);
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
    public ResponseEntity<Void> undoDeletePersonByKeycloakId(@RequestBody UndoOperationDTO undoOperationDTO) throws IllegalUndoEventPayloadPropertyException, NotFoundRevisionException, NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        UndoEvent undoEvent = undoOperationMapper.toUndoEvent(undoOperationDTO);
        personService.undoDeletePersonByKeycloakId(undoEvent);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update-person")
    @Timed(value = "person_service.update_person_time")
    public ResponseEntity<Void> updatePerson(@RequestBody UpdateIndividualDataEvent updateIndividualDataEvent) throws UserNotExistsException, CountryNotExistsException {
        Person person = individualMapper.toPerson(updateIndividualDataEvent);
        personService.updatePerson(person);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/undo-update-person")
    @Timed(value = "person_service.undo_update_person_time")
    public ResponseEntity<Void> undoUpdatePerson(@RequestBody UndoOperationDTO undoOperationDTO) throws IllegalUndoEventPayloadPropertyException, NotFoundRevisionException, NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        UndoEvent undoEvent = undoOperationMapper.toUndoEvent(undoOperationDTO);
        personService.undoUpdatePerson(undoEvent);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-person-by-keycloak-id/{keycloakId}")
    @Timed(value = "person_service.get_person_by_keycloak_id_time")
    public ResponseEntity<IndividualDataResponse> getPersonByKeycloakId(@PathVariable String keycloakId) throws UserNotExistsException {
        IndividualDataResponse response = individualMapper.toIndividualResponse(personService.getPersonByKeycloakId(keycloakId));
        return ResponseEntity.ok().body(response);
    }
}