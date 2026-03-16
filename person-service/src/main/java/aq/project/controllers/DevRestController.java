package aq.project.controllers;

import aq.project.entities.Person;
import aq.project.exceptions.UserNotExistsException;
import aq.project.repositories.PersonRepository;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
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

    public Person getByEmail(String email) throws UserNotExistsException {
        return personRepository.findByEmail(email).orElseThrow(() ->
                new UserNotExistsException(String.format("User with email [ %s ] doesn't exist", email)));
    }

    public Person getByPersonId(String personId) throws UserNotExistsException {
        return findByPersonId(personId);
    }

    @Transactional
    public void deleteByPersonId(String personId) throws UserNotExistsException {
        Person person = findByPersonId(personId);
        personRepository.delete(person);
    }

    private Person findByPersonId(String personId) throws UserNotExistsException {
        return personRepository.findById(UUID.fromString(personId)).orElseThrow(() ->
                new UserNotExistsException(String.format("User with id [ %s ] doesn't exist", personId)));
    }
}
