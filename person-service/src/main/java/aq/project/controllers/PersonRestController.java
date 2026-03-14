package aq.project.controllers;

import aq.project.dto.CreateIndividualDataRequest;
import aq.project.dto.IndividualDataResponse;
import aq.project.dto.UpdateIndividualDataRequest;
import aq.project.entities.Person;
import aq.project.exceptions.CountryNotExistsException;
import aq.project.exceptions.UserExistsException;
import aq.project.exceptions.UserNotExistsException;
import aq.project.mappers.IndividualMapper;
import aq.project.services.PersonService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/person")
public class PersonRestController {

    private final PersonService personService;
    private final IndividualMapper individualMapper;

    @PostMapping("/create")
    @Timed(value = "person_service.create_person_time")
    public ResponseEntity<String> create(@RequestBody CreateIndividualDataRequest createIndividualDataRequest) throws UserExistsException, CountryNotExistsException {
        Person person = individualMapper.toPerson(createIndividualDataRequest);
        String userId = personService.create(person);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(userId);
    }

    @DeleteMapping("/delete-by-person-id/{id}")
    @Timed(value = "person_service.delete_person_by_person_id_time")
    public ResponseEntity<Void> deleteByPersonId(@PathVariable String personId) throws UserNotExistsException {
        personService.deleteByPersonId(personId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-by-keycloak-id/{id}")
    @Timed(value = "person_service.delete_person_by_keycloak_id_time")
    public ResponseEntity<Void> deleteByKeycloakId(@PathVariable String keycloakId) throws UserNotExistsException {
        personService.deleteByKeycloakId(keycloakId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update")
    @Timed(value = "person_service.update_person_time")
    public ResponseEntity<Void> update(@RequestBody UpdateIndividualDataRequest updateIndividualDataRequest) throws UserNotExistsException, CountryNotExistsException {
        Person person = individualMapper.toPerson(updateIndividualDataRequest);
        personService.update(person);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-by-keycloak-id/{id}")
    @Timed(value = "person_service.get_person_by_keycloak_id_time")
    public ResponseEntity<IndividualDataResponse> getByKeycloakId(@PathVariable String keycloakId) throws UserNotExistsException {
        IndividualDataResponse response = individualMapper.toIndividualResponse(personService.getByKeycloakId(keycloakId));
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/get-by-person-id/{id}")
    @Timed(value = "person_service.get_person_by_person_id_time")
    public ResponseEntity<IndividualDataResponse> getByPersonId(@PathVariable String personId) throws UserNotExistsException {
        IndividualDataResponse response = individualMapper.toIndividualResponse(personService.getByPersonId(personId));
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/get-by-email/{email}")
    @Timed(value = "person_service.get_person_by_email_time")
    public ResponseEntity<IndividualDataResponse> getByEmail(@PathVariable String email) throws UserNotExistsException {
        IndividualDataResponse response = individualMapper.toIndividualResponse(personService.getByEmail(email));
        return ResponseEntity.ok().body(response);
    }
}