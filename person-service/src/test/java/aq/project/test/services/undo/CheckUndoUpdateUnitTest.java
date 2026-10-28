package aq.project.test.services.undo;

import aq.project.dto.PersonServiceOperation;
import aq.project.dto.UndoOperationStatus;
import aq.project.entities.ServiceOperation;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.ServiceOperationRepository;
import aq.project.services.UndoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static aq.project._utils.entities.UndoServiceEntities.getValidServiceOperation;

@ExtendWith(MockitoExtension.class)
public class CheckUndoUpdateUnitTest {

    @Mock
    private ServiceOperationRepository serviceOperationRepository;

    @InjectMocks
    private UndoService undoService;

    @Test
    public void successCheckUndoUpdate() {
//        Arrange
        PersonServiceOperation lastCompleteOperation = PersonServiceOperation.UPDATE_PERSON;

        ServiceOperation serviceOperation = getValidServiceOperation();
        serviceOperation.setServiceOperation(lastCompleteOperation);
        serviceOperation.setStatus(UndoOperationStatus.COMPLETED);

        Mockito.when(serviceOperationRepository.findLastServiceOperation(UndoOperationStatus.COMPLETED))
                .thenReturn(Optional.of(serviceOperation));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> undoService.checkUndoUpdate());
    }

    @Test
    public void failCheckUndoUpdateOnLastOperationIsUndoUpdate() {
//        Arrange
        PersonServiceOperation lastCompleteOperation = PersonServiceOperation.UNDO_UPDATE_PERSON;

        ServiceOperation serviceOperation = getValidServiceOperation();
        serviceOperation.setServiceOperation(lastCompleteOperation);
        serviceOperation.setStatus(UndoOperationStatus.COMPLETED);

        Mockito.when(serviceOperationRepository.findLastServiceOperation(UndoOperationStatus.COMPLETED))
                .thenReturn(Optional.of(serviceOperation));

//        Act & Assert
        Assertions.assertThrows(ProcessOperationException.class,
                () -> undoService.checkUndoUpdate());
    }

    @Test
    public void failCheckUndoUpdateOnLastOperationIsCreate() {
//        Arrange
        PersonServiceOperation lastCompleteOperation = PersonServiceOperation.CREATE_PERSON;

        ServiceOperation serviceOperation = getValidServiceOperation();
        serviceOperation.setServiceOperation(lastCompleteOperation);
        serviceOperation.setStatus(UndoOperationStatus.COMPLETED);

        Mockito.when(serviceOperationRepository.findLastServiceOperation(UndoOperationStatus.COMPLETED))
                .thenReturn(Optional.of(serviceOperation));

//        Act & Assert
        Assertions.assertThrows(ProcessOperationException.class,
                () -> undoService.checkUndoUpdate());
    }

    @Test
    public void failCheckUndoUpdateOnLastOperationIsDelete() {
//        Arrange
        PersonServiceOperation lastCompleteOperation = PersonServiceOperation.DELETE_PERSON;

        ServiceOperation serviceOperation = getValidServiceOperation();
        serviceOperation.setServiceOperation(lastCompleteOperation);
        serviceOperation.setStatus(UndoOperationStatus.COMPLETED);

        Mockito.when(serviceOperationRepository.findLastServiceOperation(UndoOperationStatus.COMPLETED))
                .thenReturn(Optional.of(serviceOperation));

//        Act & Assert
        Assertions.assertThrows(ProcessOperationException.class,
                () -> undoService.checkUndoUpdate());
    }
}
