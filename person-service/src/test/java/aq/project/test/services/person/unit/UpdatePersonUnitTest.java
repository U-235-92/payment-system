package aq.project.test.services.person.unit;

import aq.project.dto.PersonServiceUpdatePersonRequestDto;
import aq.project.entities.Country;
import aq.project.entities.Person;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.services.PersonService;
import aq.project.utils.mappers.UpdateCountryMapper;
import aq.project.utils.mappers.UpdatePersonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.entities.PersonServiceEntities.*;

@ExtendWith(MockitoExtension.class)
public class UpdatePersonUnitTest {

    @Spy
    private final UpdatePersonMapper updatePersonMapper = UpdatePersonMapper.INSTANCE;
    @Spy
    private final UpdateCountryMapper updateCountryMapper = UpdateCountryMapper.INSTANCE;

    @Mock
    private PersonRepository personRepository;
    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private PersonService personService;

    @Test
    void successUpdatePerson() {
//        Arrange
        String email = "test@mail.aq";

        UUID personId = UUID.randomUUID();
        UUID personKeycloakId = UUID.randomUUID();

        Person person = getValidPerson();
        person.setId(personId);
        person.setKeycloakId(personKeycloakId.toString());
        person.getIndividual().setEmail(email);

        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getValidPersonServiceUpdatePersonRequestDto();
        personServiceUpdatePersonRequestDto.setPersonKeycloakId(personKeycloakId);

        Mockito.when(personRepository.findByKeycloakId(Mockito.any()))
                .thenReturn(Optional.of(person));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService.updatePerson(personServiceUpdatePersonRequestDto));
    }

    @Test
    public void failUpdatePersonOnPersonNotFound() {
//        Arrange
        PersonServiceUpdatePersonRequestDto personServiceUpdatePersonRequestDto = getValidPersonServiceUpdatePersonRequestDto();

        Country country = getValidCountry();

        Mockito.when(personRepository.findByKeycloakId(Mockito.any()))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> personService.updatePerson(personServiceUpdatePersonRequestDto));

        Mockito.verify(personRepository, Mockito.never())
                .save(Mockito.any(Person.class));
    }
}
