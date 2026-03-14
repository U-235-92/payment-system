package aq.project.services;

import aq.project.entities.Person;
import aq.project.exceptions.CountryNotExistsException;
import aq.project.exceptions.UserExistsException;
import aq.project.exceptions.UserNotExistsException;
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

    private final PersonRepository personRepository;
    private final CountryRepository countryRepository;

    @Transactional
    public String create(Person person) throws UserExistsException, CountryNotExistsException {
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

    public Person getByEmail(String email) throws UserNotExistsException {
        return personRepository.findByEmail(email).orElseThrow(() ->
                new UserNotExistsException(String.format("User with email [ %s ] doesn't exist", email)));
    }

    public Person getByKeycloakId(String keycloakId) throws UserNotExistsException {
        return findByKeycloakId(keycloakId);
    }

    public Person getByPersonId(String personId) throws UserNotExistsException {
        return findByPersonId(personId);
    }

    @Transactional
    public void update(Person from) throws UserNotExistsException, CountryNotExistsException {
        String countryCode = from.getAddress().getCountry().getCode();
        if(isCountryNotExists(countryCode)) {
            throw new CountryNotExistsException(String.format("Country with code [ %s ] doesn't exist", countryCode));
        }
        Person to = findByKeycloakId(from.getKeycloakId());
        to.getAddress().setCountry(countryRepository.findByCountryCode(countryCode).get());
        PersonMapper.map(from, to);
        personRepository.save(to);
    }

    @Transactional
    public void deleteByPersonId(String personId) throws UserNotExistsException {
        Person person = findByPersonId(personId);
        personRepository.delete(person);
    }

    @Transactional
    public void deleteByKeycloakId(String keycloakId) throws UserNotExistsException {
        Person person = findByKeycloakId(keycloakId);
        personRepository.delete(person);
    }

    private Person findByPersonId(String personId) throws UserNotExistsException {
        return personRepository.findById(UUID.fromString(personId)).orElseThrow(() ->
                new UserNotExistsException(String.format("User with id [ %s ] doesn't exist", personId)));
    }

    private Person findByKeycloakId(String keycloakId) throws UserNotExistsException {
        return personRepository.findByKeycloakId(keycloakId).orElseThrow(() ->
                new UserNotExistsException(String.format("User with id [ %s ] doesn't exist", keycloakId)));
    }

    private boolean isCountryNotExists(String countryCode) {
        return countryRepository.findByCountryCode(countryCode).isEmpty();
    }
}
