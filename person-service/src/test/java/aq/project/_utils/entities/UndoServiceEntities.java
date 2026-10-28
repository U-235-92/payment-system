package aq.project._utils.entities;

import aq.project.dto.PersonServiceOperation;
import aq.project.dto.UndoOperationStatus;
import aq.project.entities.ServiceOperation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class UndoServiceEntities {

    public static ServiceOperation getValidServiceOperation() {
        ServiceOperation operation = new ServiceOperation();
        operation.setId(1);
        operation.setServiceOperation(PersonServiceOperation.CREATE_PERSON);
        operation.setTimestamp(OffsetDateTime.now());
        operation.setDescription("Description");
        operation.setStatus(UndoOperationStatus.COMPLETED);
        return operation;
    }
}
