package aq.project.util.entity;

import aq.project.entities.UndoEvent;
import aq.project.util.constants.Operations;

import java.util.UUID;

public abstract class Events {

    public static UndoEvent getInvalidUndoEvent() {
        UndoEvent undoEvent = new UndoEvent();
        undoEvent.setPersonKeycloakId(UUID.randomUUID());
        undoEvent.setOperation("wrong_operation");
        undoEvent.setTimestamp(-1L);
        return undoEvent;
    }

    public static UndoEvent getValidUndoEvent() {
        UndoEvent undoEvent = new UndoEvent();
        undoEvent.setPersonKeycloakId(UUID.randomUUID());
        undoEvent.setOperation(Operations.UNDO_UPDATE_PERSON_OP);
        undoEvent.setTimestamp(System.currentTimeMillis());
        return undoEvent;
    }
}
