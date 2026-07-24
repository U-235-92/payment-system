package aq.project.controllers;

import aq.project.controller.PersonRestControllerApi;
import aq.project.dto.CreateIndividualDataDto;
import aq.project.dto.IndividualDataResponseDto;
import aq.project.dto.UndoOperationDto;
import aq.project.dto.UpdateIndividualDataDto;
import aq.project.entities.Person;
import aq.project.entities.UndoOperation;
import aq.project.mappers.IndividualDataDtoMapper;
import aq.project.mappers.UndoOperationDtoMapper;
import aq.project.services.PersonService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PersonRestController implements PersonRestControllerApi {

    private final PersonService personService;

    private final UndoOperationDtoMapper undoOperationDtoMapper;
    private final IndividualDataDtoMapper individualDataDtoMapper;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<String> createPerson(
            String xTraceId,
            CreateIndividualDataDto createIndividualDataDTO,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        Person person = individualDataDtoMapper.toPerson(createIndividualDataDTO);
        String userId = personService.createPerson(person);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(userId);
    }

    @Override
    public ResponseEntity<Void> deletePersonByKeycloakId(
            String keycloakId,
            String xTraceId,
            String authorization
    )  {
        traceContext.setTraceId(xTraceId);
        personService.deletePersonByKeycloakId(keycloakId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> undoDeletePerson(
            String xTraceId,
            UndoOperationDto undoOperationDTO,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        UndoOperation undoOperation = undoOperationDtoMapper.toUndoOperation(undoOperationDTO);
        personService.undoDeletePerson(undoOperation);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updatePerson(
            String xTraceId,
            UpdateIndividualDataDto updateIndividualDataDTO,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        Person person = individualDataDtoMapper.toPerson(updateIndividualDataDTO);
        personService.updatePerson(person);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> undoUpdatePerson(
            String xTraceId,
            UndoOperationDto undoOperationDTO,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        UndoOperation undoOperation = undoOperationDtoMapper.toUndoOperation(undoOperationDTO);
        personService.undoUpdatePerson(undoOperation);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<IndividualDataResponseDto> getPersonByKeycloakId(
            String keycloakId,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);
        IndividualDataResponseDto response = individualDataDtoMapper.toIndividualResponseDTO(personService.getPersonByKeycloakId(keycloakId));
        return ResponseEntity.ok().body(response);
    }
}