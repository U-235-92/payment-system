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
            CreatePersonRequestDto createPersonRequestDto,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        UUID userId = personService.createPerson(createPersonRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(userId);
    }

    @Override
    public ResponseEntity<Void> deletePerson(
            UUID personKeycloakId,
            String xTraceId,
            String authorization
    )  {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        personService.deletePerson(personKeycloakId);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> undoDeletePerson(
            String xTraceId,
            UndoDeletePersonRequestDto undoDeletePersonRequestDto,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        personService.undoDeletePerson(undoDeletePersonRequestDto);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updatePerson(
            String xTraceId,
            UpdatePersonRequestDto updatePersonRequestDto,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        personService.updatePerson(updatePersonRequestDto);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> undoUpdatePerson(
            String xTraceId,
            UndoUpdatePersonRequestDto undoUpdatePersonRequestDto,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        personService.undoUpdatePerson(undoUpdatePersonRequestDto);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PersonInfoResponseDto> getPersonInfo(
            UUID personKeycloakId,
            String xTraceId,
            String authorization
    ) {
        traceContext.clean();
        traceContext.setTraceId(xTraceId);

        return ResponseEntity.ok().body(personService.getPersonInfo(personKeycloakId));
    }
}