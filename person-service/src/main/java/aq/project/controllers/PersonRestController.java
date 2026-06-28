package aq.project.controllers;

import aq.project.controller.PersonRestControllerApi;
import aq.project.dto.CreateIndividualDataDTO;
import aq.project.dto.ResponseIndividualDataDTO;
import aq.project.dto.UndoOperationDTO;
import aq.project.dto.UpdateIndividualDataDTO;
import aq.project.entities.Person;
import aq.project.entities.UndoOperation;
import aq.project.mappers.IndividualDataDtoMapper;
import aq.project.mappers.UndoOperationDtoMapper;
import aq.project.services.PersonService;
import aq.project.util.telemetry.TraceContext;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PersonRestController implements PersonRestControllerApi {

    private final PersonService personService;

    private final UndoOperationDtoMapper undoOperationDtoMapper;
    private final IndividualDataDtoMapper individualDataDtoMapper;

    private final TraceContext traceContext;

    @Override
    @Timed(value = "person_service.create_person_time")
    public ResponseEntity<String> createPerson(String xTraceId, CreateIndividualDataDTO createIndividualDataDTO) {
        traceContext.setTraceId(xTraceId);
        Person person = individualDataDtoMapper.toPerson(createIndividualDataDTO);
        String userId = personService.createPerson(person);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(userId);
    }

    @Override
    @Timed(value = "person_service.delete_person_by_keycloak_id_time")
    public ResponseEntity<Void> deletePersonByKeycloakId(String keycloakId, String xTraceId)  {
        traceContext.setTraceId(xTraceId);
        personService.deletePersonByKeycloakId(keycloakId);
        return ResponseEntity.ok().build();
    }

    @Override
    @Timed(value = "person_service.undo_delete_person_by_keycloak_id_time")
    public ResponseEntity<Void> undoDeletePerson(String xTraceId, UndoOperationDTO undoOperationDTO) {
        traceContext.setTraceId(xTraceId);
        UndoOperation undoOperation = undoOperationDtoMapper.toUndoOperation(undoOperationDTO);
        personService.undoDeletePerson(undoOperation);
        return ResponseEntity.ok().build();
    }

    @Override
    @Timed(value = "person_service.update_person_time")
    public ResponseEntity<Void> updatePerson(String xTraceId, UpdateIndividualDataDTO updateIndividualDataDTO) {
        traceContext.setTraceId(xTraceId);
        Person person = individualDataDtoMapper.toPerson(updateIndividualDataDTO);
        personService.updatePerson(person);
        return ResponseEntity.ok().build();
    }

    @Override
    @Timed(value = "person_service.undo_update_person_time")
    public ResponseEntity<Void> undoUpdatePerson(String xTraceId, UndoOperationDTO undoOperationDTO) {
        traceContext.setTraceId(xTraceId);
        UndoOperation undoOperation = undoOperationDtoMapper.toUndoOperation(undoOperationDTO);
        personService.undoUpdatePerson(undoOperation);
        return ResponseEntity.ok().build();
    }

    @Override
    @Timed(value = "person_service.get_person_by_keycloak_id_time")
    public ResponseEntity<ResponseIndividualDataDTO> getPersonByKeycloakId(String keycloakId, String xTraceId) {
        traceContext.setTraceId(xTraceId);
        ResponseIndividualDataDTO response = individualDataDtoMapper.toIndividualResponseDTO(personService.getPersonByKeycloakId(keycloakId));
        return ResponseEntity.ok().body(response);
    }
}