package aq.project.services;

import aq.project.dto.*;
import aq.project.entities.Country;
import aq.project.entities.Person;
import aq.project.exceptions.EntityAlreadyExistException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.utils.mappers.CreatePersonMapper;
import aq.project.utils.mappers.UpdateCountryMapper;
import aq.project.utils.mappers.UpdatePersonMapper;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final CreatePersonMapper createPersonMapper = CreatePersonMapper.INSTANCE;
    private final UpdatePersonMapper updatePersonMapper = UpdatePersonMapper.INSTANCE;
    private final UpdateCountryMapper updateCountryMapper = UpdateCountryMapper.INSTANCE;

    private final UndoService undoService;

    private final PersonRepository personRepository;
    private final CountryRepository countryRepository;

    private final Fallback fallback;

    @RateLimiter(
            name = "create-person-rate-limiter",
            fallbackMethod = "createPersonRateLimiterFallback"
    )
    @Transactional
    public UUID createPerson(
            PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto
    ) {
        String email = personServiceCreatePersonRequestDto.getEmail();

        if(personRepository.findByEmail(email).isPresent())
            throw new EntityAlreadyExistException(
                    String.format("Person with email: [%s] is already exists", email));

        Person person = createPersonMapper.toPerson(personServiceCreatePersonRequestDto);

        String requestCountryCode = personServiceCreatePersonRequestDto.getCountry().getCode();

        Optional<Country> optionalCountry = countryRepository.findByCountryCode(requestCountryCode);

        if(optionalCountry.isPresent()) {
            Country country = optionalCountry.get();
            person.setCountry(country);
        } else {
            Country newCountry = person.getCountry();
            Country savedCountry = countryRepository.save(newCountry);
            person.setCountry(savedCountry);
        }
        person.setActive(true);

        return personRepository.save(person).getId();
    }

    private UUID createPersonRateLimiterFallback(
            PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityAlreadyExistException)
            throw exception;

        String action = "create-person";
        String email = personServiceCreatePersonRequestDto.getEmail();
        String message = String.format(
                "Exception occurred during creating of person with email: [%s]. Exception message: %s",
                email, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);

        return null;
    }

    @RateLimiter(
            name = "update-person-rate-limiter",
            fallbackMethod = "updatePersonRateLimiterFallback"
    )
    @Transactional
    public void updatePerson(
            PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto
    ) {
        String personKeycloakId = personServiceUpdatePersonRequestDto.getPersonKeycloakId().toString();

        Person currentPerson = personRepository.findByKeycloakId(personKeycloakId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Person with keycloak id: [%s] doesn't exist", personKeycloakId)));

        updatePersonMapper.updatePersonFromDto(personServiceUpdatePersonRequestDto, currentPerson);

        if(personServiceUpdatePersonRequestDto.getCountry() != null) {
            PersonServiceCountryDto countryDto = personServiceUpdatePersonRequestDto.getCountry();
            String requestCountryCode = countryDto.getCode();
            Country currentCountry = createCountryIfNotExist(requestCountryCode, countryDto);
            currentPerson.setCountry(currentCountry);
        }
    }

    private void updatePersonRateLimiterFallback(
            PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "update-person";
        UUID personKeycloakId = personServiceUpdatePersonRequestDto.getPersonKeycloakId();
        String message = String.format(
                "Exception occurred during update of person with keycloak id: [%s]. Exception message: %s",
                personKeycloakId, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);
    }

    private Country createCountryIfNotExist(
            String countryCode,
            PersonServiceCountryDto dto
    ) {
        return countryRepository.findByCountryCode(countryCode)
                .orElseGet(() -> {
                   Country country = new Country();
                    updateCountryMapper.updateCountryFromDto(dto, country);
                   return countryRepository.save(country);
                });
    }

    @RateLimiter(
            name = "undo-update-person-rate-limiter",
            fallbackMethod = "undoUpdatePersonRateLimiterFallback"
    )
    @Transactional
    public void undoUpdatePerson(
            PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto
    ) {
        UUID personKeycloakId = personServiceUndoUpdatePersonRequestDto.getPersonKeycloakId();

        undoService.checkUndoUpdate();

        undoService.undoUpdatePerson(personKeycloakId);
    }

    private void undoUpdatePersonRateLimiterFallback(
            PersonServiceUndoUpdatePersonRequestDto personServiceUndoUpdatePersonRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof ProcessOperationException
                || exception instanceof EntityNotFoundException)
            throw exception;

        String action = "undo-update-person";
        UUID personKeycloakId = personServiceUndoUpdatePersonRequestDto.getPersonKeycloakId();
        String message = String.format(
                "Exception occurred during undo update of person with keycloak id: [%s]. Exception message: %s",
                personKeycloakId, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);
    }

    @RateLimiter(
            name = "delete-person-rate-limiter",
            fallbackMethod = "deletePersonRateLimiterFallback"
    )
    @Transactional
    public void deletePerson(
            UUID personKeycloakId
    ) {
        Person person = personRepository.findByKeycloakId(personKeycloakId.toString())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Person with keycloak id: [%s] doesn't exist", personKeycloakId)));

        personRepository.delete(person);
    }

    private void deletePersonRateLimiterFallback(
            UUID personKeycloakId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "delete-person";
        String message = String.format(
                "Exception occurred during delete of person with keycloak id: [%s]. Exception message: %s",
                personKeycloakId, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);
    }

    @RateLimiter(
            name = "undo-delete-person-rate-limiter",
            fallbackMethod = "undoDeletePersonRateLimiterFallback"
    )
    @Transactional
    public void undoDeletePerson(
            PersonServiceUndoDeletePersonRequestDto personServiceUndoDeletePersonRequestDto
    ) {
        UUID personKeycloakId = personServiceUndoDeletePersonRequestDto.getPersonKeycloakId();

        undoService.checkUndoDelete();

        undoService.undoDeletePerson(personKeycloakId);
    }

    private void undoDeletePersonRateLimiterFallback(
            PersonServiceUndoDeletePersonRequestDto personServiceUndoDeletePersonRequestDto,
            Exception exception
    ) throws Exception {
        if(exception instanceof ProcessOperationException
                || exception instanceof EntityNotFoundException)
            throw exception;

        String action = "undo-delete-person";
        UUID personKeycloakId = personServiceUndoDeletePersonRequestDto.getPersonKeycloakId();
        String message = String.format(
                "Exception occurred during undo delete of person with keycloak id: [%s]. Exception message: %s",
                personKeycloakId, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);
    }

    @RateLimiter(
            name = "get-person-info-rate-limiter",
            fallbackMethod = "getPersonInfoRateLimiterFallback"
    )
    public PersonServicePersonInfoResponseDto getPersonInfo(
            UUID personKeycloakId
    ) {
        Person person = personRepository.findByKeycloakId(personKeycloakId.toString())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Person with keycloak id: [%s] doesn't exist", personKeycloakId)));

        return createPersonMapper.toPersonServicePersonInfoResponseDto(person);
    }

    private void getPersonInfoRateLimiterFallback(
            UUID personKeycloakId,
            Exception exception
    ) throws Exception {
        if(exception instanceof EntityNotFoundException)
            throw exception;

        String action = "get-person-info";
        String message = String.format(
                "Exception occurred during getting info of person with keycloak id: [%s]. Exception message: %s",
                personKeycloakId, exception.getMessage());

        fallback.handleBulkheadFallback(action, message, exception);
    }
}