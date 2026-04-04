package aq.project.util.entity;

import aq.project.entities.UndoOperation;
import aq.project.util.constants.Operations;

import java.util.UUID;

public abstract class Events {

    public static UndoOperation getInvalidUndoOperation() {
        UndoOperation undoOperation = new UndoOperation();
        undoOperation.setPersonKeycloakId(UUID.randomUUID());
        undoOperation.setOperation("wrong_operation");
        undoOperation.setTimestamp(-1L);
        return undoOperation;
    }

    public static UndoOperation getValidUndoOperation() {
        UndoOperation undoOperation = new UndoOperation();
        undoOperation.setPersonKeycloakId(UUID.randomUUID());
        undoOperation.setOperation(Operations.UNDO_UPDATE_PERSON_OP);
        undoOperation.setTimestamp(System.currentTimeMillis());
        return undoOperation;
    }
}
