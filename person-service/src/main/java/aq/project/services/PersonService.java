package aq.project.services;

import aq.project.entities.Person;
import aq.project.entities.UndoEvent;
import aq.project.exceptions.*;
import aq.project.mappers.PersonMapper;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final UndoService undoService;

    private final PersonRepository personRepository;
    private final CountryRepository countryRepository;

    @Transactional
    public String createPerson(Person person) throws UserExistsException, CountryNotExistsException {
        String email = person.getIndividual().getEmail();
        if(personRepository.findByEmail(email).isPresent()) {
            throw new UserExistsException(String.format("User with email [ %s ] is already exists", email));
        }
        String countryCode = person.getAddress().getCountry().getCode();
        if(isCountryNotExists(countryCode)) {
            throw new CountryNotExistsException(String.format("Country with code [ %s ] doesn't exist", countryCode));
        }
        person.setActive(true);
        person.getAddress().setCountry(countryRepository.findByCountryCode(countryCode).get());
        Person saved = personRepository.save(person);
        return saved.getId().toString();
    }

    public Person getPersonByKeycloakId(String keycloakId) throws UserNotExistsException {
        return findPersonByKeycloakId(keycloakId);
    }

    @Transactional
    public void updatePerson(Person from) throws UserNotExistsException, CountryNotExistsException {
        String countryCode = from.getAddress().getCountry().getCode();
        if(isCountryNotExists(countryCode)) {
            throw new CountryNotExistsException(String.format("Country with code [ %s ] doesn't exist", countryCode));
        }
        Person to = findPersonByKeycloakId(from.getKeycloakId());
        to.getAddress().setCountry(countryRepository.findByCountryCode(countryCode).get());
        PersonMapper.map(from, to);
        personRepository.save(to);
    }

    @Transactional
    public void undoUpdatePerson(UndoEvent undoEvent) throws NotFoundRevisionException, NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        UUID undoEventId = undoService.saveUndoEvent(undoEvent);
        undoService.undoOperation(undoEvent.getPersonKeycloakId().toString(), undoEventId, undoEvent.getOperation());
    }

    @Transactional
    public void deletePersonByKeycloakId(String keycloakId) throws UserNotExistsException {
        Person person = findPersonByKeycloakId(keycloakId);
        personRepository.delete(person);
    }

    @Transactional
    public void undoDeletePersonByKeycloakId(UndoEvent undoEvent) throws NotFoundRevisionException, NotExpectedUndoOperationCallException, NotFoundUndoOperationCallException {
        UUID undoEventId = undoService.saveUndoEvent(undoEvent);
        undoService.undoOperation(undoEvent.getPersonKeycloakId().toString(), undoEventId, undoEvent.getOperation());
    }

    private Person findPersonByKeycloakId(String keycloakId) throws UserNotExistsException {
        return personRepository.findByKeycloakId(keycloakId).orElseThrow(() ->
                new UserNotExistsException(String.format("Person with keycloak id [ %s ] doesn't exist", keycloakId)));
    }

    private boolean isCountryNotExists(String countryCode) {
        return countryRepository.findByCountryCode(countryCode).isEmpty();
    }
}
