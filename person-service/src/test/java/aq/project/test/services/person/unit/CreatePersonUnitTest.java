package aq.project.test.services.person.unit;

import aq.project.dto.PersonServiceCreatePersonRequestDto;
import aq.project.entities.Country;
import aq.project.entities.Person;
import aq.project.exceptions.EntityAlreadyExistException;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import aq.project.services.PersonService;
import aq.project.utils.mappers.CreatePersonMapper;
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
public class CreatePersonUnitTest {

    @Spy
    private final CreatePersonMapper createPersonMapper = CreatePersonMapper.INSTANCE;

    @Mock
    private PersonRepository personRepository;
    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private PersonService personService;

    @Test
    public void successCreatePerson() {
//        Arrange
        String email = "test@mail.aq";

        UUID personId = UUID.randomUUID();

        Person person = getValidPerson();
        person.setId(personId);
        person.getIndividual().setEmail(email);

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setEmail(email);

        Country country = getValidCountry();

        Mockito.when(personRepository.findByEmail(Mockito.any()))
                .thenReturn(Optional.empty());
        Mockito.when(countryRepository.findByCountryCode(Mockito.any()))
                .thenReturn(Optional.of(country));
        Mockito.when(personRepository.save(Mockito.any(Person.class)))
                .thenReturn(person);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService.createPerson(personServiceCreatePersonRequestDto));

        Mockito.verify(personRepository, Mockito.times(1))
                .save(Mockito.any(Person.class));
    }

    @Test
    public void failCreatePersonOnPersonWithEmailAlreadyExists() {
//        Arrange
        String email = "test@mail.aq";

        PersonServiceCreatePersonRequestDto personServiceCreatePersonRequestDto = getValidPersonServiceCreatePersonRequestDto();
        personServiceCreatePersonRequestDto.setEmail(email);

        Person person = getValidPerson();
        person.getIndividual().setEmail(email);

        Mockito.when(personRepository.findByEmail(Mockito.any()))
                .thenReturn(Optional.of(person));

//        Act & Assert
        Assertions.assertThrows(EntityAlreadyExistException.class,
                () -> personService.createPerson(personServiceCreatePersonRequestDto));

        Mockito.verify(personRepository, Mockito.never())
                .save(Mockito.any(Person.class));
    }
}
