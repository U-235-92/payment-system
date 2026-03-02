package aq.project.controllers;

import aq.project.dto.IndividualRequest;
import aq.project.dto.IndividualResponse;
import aq.project.entities.Person;
import aq.project.exceptions.CountryNotExistsException;
import aq.project.exceptions.UserExistsException;
import aq.project.exceptions.UserNotExistsException;
import aq.project.mappers.IndividualMapper;
import aq.project.services.PersonService;
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
    public ResponseEntity<String> create(@RequestBody IndividualRequest request) throws UserExistsException, CountryNotExistsException {
        Person person = individualMapper.toPerson(request);
        String userId = personService.create(person);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(userId);
    }

    @DeleteMapping("/undo-create/{id}")
    public ResponseEntity<Void> undoCreate(@PathVariable String id) throws UserNotExistsException {
        personService.delete(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) throws UserNotExistsException {
        personService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<Void> update(@RequestBody IndividualRequest request, @PathVariable String id) throws UserNotExistsException, CountryNotExistsException {
        Person person = individualMapper.toPerson(request);
        personService.update(id, person);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<IndividualResponse> getById(@PathVariable String id) throws UserNotExistsException {
        IndividualResponse response = individualMapper.toIndividualResponse(personService.getById(id));
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/get/{email}")
    public ResponseEntity<IndividualResponse> getByEmail(@PathVariable String email) throws UserNotExistsException {
        IndividualResponse response = individualMapper.toIndividualResponse(personService.getByEmail(email));
        return ResponseEntity.ok().body(response);
    }
}