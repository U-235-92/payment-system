package aq.project.test.services.person.unit;

import aq.project.entities.Person;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.PersonRepository;
import aq.project.services.PersonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.entities.PersonServiceEntities.getValidPerson;

@ExtendWith(MockitoExtension.class)
public class DeletePersonUnitTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    @Test
    public void successDeletePerson() {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        Person person = getValidPerson();
        person.setKeycloakId(personKeycloakId.toString());

        Mockito.when(personRepository.findByKeycloakId(Mockito.any()))
                .thenReturn(Optional.of(person));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService.deletePerson(personKeycloakId));

        Mockito.verify(personRepository, Mockito.times(1))
                .delete(Mockito.any(Person.class));
    }

    @Test
    public void failDeletePersonOnPersonNotFound() {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        Mockito.when(personRepository.findByKeycloakId(Mockito.any()))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> personService.deletePerson(personKeycloakId));

        Mockito.verify(personRepository, Mockito.never())
                .delete(Mockito.any(Person.class));
    }
}
