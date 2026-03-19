package aq.project.controllers;

import aq.project.entities.Person;
import aq.project.exceptions.NotFoundRevisionException;
import aq.project.exceptions.UserNotExistsException;
import aq.project.repositories.PersonRepository;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Profile("dev")
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevRestController {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    private final PersonRepository personRepository;

    @GetMapping("/check-telemetry")
    public ResponseEntity<String> checkTelemetry(@RequestParam String name) {
        Tracer tracer = openTelemetry.getTracer( applicationName + ".hello-tracer");
        Span span = tracer.spanBuilder("hello-span").startSpan();
        span.setAttribute("person-name", name);
        String traceId = span.getSpanContext().getTraceId();
        String spanId = span.getSpanContext().getSpanId();
        String logMessage = String.format("[%s-%s] checkTelemetry method called with param: %s", traceId, spanId, name);
        log.info(logMessage);
        span.end();
//        throw new RuntimeException("Oops...");
        return ResponseEntity.ok("Hello, " + name);
    }

    @GetMapping("/get-person-by-email/{email}")
    public Person getByEmail(@PathVariable String email) throws UserNotExistsException {
        return personRepository.findByEmail(email).orElseThrow(() ->
                new UserNotExistsException(String.format("User with email [ %s ] doesn't exist", email)));
    }

    @GetMapping("/get-person-by-person-id/{personId}")
    public Person getByPersonId(@PathVariable String personId) throws UserNotExistsException {
        return findByPersonId(personId);
    }

    @Transactional
    @DeleteMapping("/delete-person-by-person-id/{personId}")
    public void deleteByPersonId(@PathVariable String personId) throws UserNotExistsException {
        Person person = findByPersonId(personId);
        personRepository.delete(person);
    }

    private Person findByPersonId(String personId) throws UserNotExistsException {
        return personRepository.findById(UUID.fromString(personId)).orElseThrow(() ->
                new UserNotExistsException(String.format("User with id [ %s ] doesn't exist", personId)));
    }

    @GetMapping("/do-void-call/{msg}")
    public void doReturnVoidCall(@PathVariable String msg) {
        log.info("doReturnVoidCall msg = " + msg);
    }

    @GetMapping("/get-person-revision-by-person-id/{keycloakId}")
    public List<Person> getPersonRevisionsByPersonId(@PathVariable String keycloakId) {
        Revisions<Integer, Person> revisions = personRepository.findRevisions(UUID.fromString(keycloakId));
        return revisions.stream().map(Revision::getEntity).toList();
    }

    @GetMapping("/get-person-revision-by-keycloak-id/{keycloakId}")
    public List<Person> getPersonRevisionsByKeycloakId(@PathVariable String keycloakId) {
        return personRepository.findRevisionsByKeycloakId(keycloakId);
    }

    @GetMapping("/get-last-person-revision-by-keycloak-id/{keycloakId}")
    public Person getLastPersonRevisionsByKeycloakId(@PathVariable String keycloakId) throws NotFoundRevisionException {
        return personRepository.findLastRevisionByKeycloakId(keycloakId);
    }

    @GetMapping("/get-undo-person-revision-by-keycloak-id/{keycloakId}")
    public Person getUndoPersonRevisionsByKeycloakId(@PathVariable String keycloakId) throws NotFoundRevisionException {
        return personRepository.findUndoRevisionByKeycloakId(keycloakId);
    }
}
