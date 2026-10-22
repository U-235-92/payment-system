package aq.project.controllers;

import aq.project.controller.PersonRestControllerApi;
import aq.project.dto.*;
import aq.project.services.PersonService;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PersonRestController implements PersonRestControllerApi {

    private final PersonService personService;

    private final TraceContext traceContext;

    @Override
    public ResponseEntity<UUID> createPerson(
            String xTraceId,
            PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);

        UUID userId = personService.createPerson(personServiceCreatePersonRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(userId);
    }

    @Override
    public ResponseEntity<Void> deletePerson(
            UUID personKeycloakId,
            String xTraceId,
            String authorization
    )  {
        traceContext.setTraceId(xTraceId);

        personService.deletePerson(personKeycloakId);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> undoDeletePerson(
            String xTraceId,
            PersonServiceUndoDeletePersonRequestDto personServiceUndoDeletePersonRequestDto,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);

        personService.undoDeletePerson(personServiceUndoDeletePersonRequestDto);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updatePerson(
            String xTraceId,
            PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);

        personService.updatePerson(personServiceUpdatePersonRequestDto);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> undoUpdatePerson(
            String xTraceId,
            PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);

        personService.undoUpdatePerson(personServiceUndoUpdatePersonRequestDto);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PersonServicePersonInfoResponseDto> getPersonInfo(
            UUID personKeycloakId,
            String xTraceId,
            String authorization
    ) {
        traceContext.setTraceId(xTraceId);

        return ResponseEntity.ok().body(personService.getPersonInfo(personKeycloakId));
    }
}