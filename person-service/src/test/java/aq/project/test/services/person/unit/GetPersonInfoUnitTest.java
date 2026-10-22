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
public class GetPersonInfoUnitTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    @Test
    public void successGetPersonInfo() {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        Person person = getValidPerson();
        person.setKeycloakId(personKeycloakId.toString());

        Mockito.when(personRepository.findByKeycloakId(Mockito.any()))
                .thenReturn(Optional.of(person));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> personService.getPersonInfo(personKeycloakId));
    }

    @Test
    public void failGetPersonInfoOnPersonNotFound() {
//        Arrange
        UUID personKeycloakId = UUID.randomUUID();

        Person person = getValidPerson();
        person.setKeycloakId(personKeycloakId.toString());

        Mockito.when(personRepository.findByKeycloakId(Mockito.any()))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> personService.getPersonInfo(personKeycloakId));
    }
}
