package aq.project.services;

import aq.project.entities.Person;
import aq.project.exceptions.CountryNotExistsException;
import aq.project.exceptions.UserExistsException;
import aq.project.exceptions.UserNotExistsException;
import aq.project.mappers.PersonMapper;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final CountryRepository countryRepository;

    @Transactional
    public String create(@Valid Person person) throws UserExistsException, CountryNotExistsException {
        String email = person.getIndividual().getEmail();
        if(personRepository.findByEmail(email).isPresent()) {
            throw new UserExistsException(String.format("User with email [ %s ] is already exists", email));
        }
        String countryCode = person.getAddress().getCountry().getCode();
        if(isCountryNotExists(countryCode)) {
            throw new CountryNotExistsException(String.format("Country with code [ %s ] doesn't exist", countryCode));
        }
        Person saved = personRepository.save(person);
        return saved.getId().toString();
    }

    public Person getByEmail(@Email String email) throws UserNotExistsException {
        return personRepository.findByEmail(email).orElseThrow(() ->
                new UserNotExistsException(String.format("User with email [ %s ] doesn't exist", email)));
    }

    public Person getById(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String id) throws UserNotExistsException {
        return findById(id);
    }

    @Transactional
    public void update(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String id, @Valid Person from) throws UserNotExistsException, CountryNotExistsException {
        String countryCode = from.getAddress().getCountry().getCode();
        if(isCountryNotExists(countryCode)) {
            throw new CountryNotExistsException(String.format("Country with code [ %s ] doesn't exist", countryCode));
        }
        Person to = findById(id);
        PersonMapper.map(from, to);
        personRepository.save(to);
    }

    @Transactional
    public void delete(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String id) throws UserNotExistsException {
        Person person = findById(id);
        personRepository.delete(person);
    }

    private Person findById(String id) throws UserNotExistsException {
        return personRepository.findById(UUID.fromString(id)).orElseThrow(() ->
                new UserNotExistsException(String.format("User with id [ %s ] doesn't exist", id)));
    }

    private boolean isCountryNotExists(String countryCode) {
        return countryRepository.findByCountryCode(countryCode).isEmpty();
    }
}
