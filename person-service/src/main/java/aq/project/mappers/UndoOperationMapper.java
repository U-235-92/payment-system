package aq.project.mappers;

import aq.project.dto.UndoOperationDTO;
import aq.project.entities.UndoEvent;
import aq.project.exceptions.IllegalUndoEventPayloadPropertyException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class UndoOperationMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "personKeycloakId", expression = "java(toPersonKeycloakId(event))")
    @Mapping(target = "operation", expression = "java(toOperation(event))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(event))")
    @Mapping(target = "description", expression = "java(toDescription(event))")
    public abstract UndoEvent toUndoEvent(UndoOperationDTO event) throws IllegalUndoEventPayloadPropertyException;

    @Named("toPersonKeycloakId")
    protected UUID toPersonKeycloakId(UndoOperationDTO event) throws IllegalUndoEventPayloadPropertyException {
        if(event.getPayload().get("person-keycloak-id") == null)
            throw getIllegalUndoEventPayloadPropertyException("person-keycloak-id", "null");
        return UUID.fromString(event.getPayload().get("person-keycloak-id"));
    }

    @Named("toOperation")
    protected String toOperation(UndoOperationDTO event) {
        return event.getOperation().getValue();
    }

    @Named("toTimestamp")
    protected long toTimestamp(UndoOperationDTO event) throws IllegalUndoEventPayloadPropertyException {
        if(event.getPayload().get("timestamp") == null)
            throw getIllegalUndoEventPayloadPropertyException("timestamp", "null");
        String created = event.getPayload().get("timestamp");
        return Long.parseLong(created);
    }

    @Named("toDescription")
    protected String toDescription(UndoOperationDTO event) throws IllegalUndoEventPayloadPropertyException {
        if(event.getPayload().get("description") == null)
            throw getIllegalUndoEventPayloadPropertyException("description", "null");
        return event.getPayload().get("description");
    }

    private IllegalUndoEventPayloadPropertyException getIllegalUndoEventPayloadPropertyException(String propertyName, String propertyValue) {
        String msg = String.format("Illegal UndoEvent payload property: %s=%s", propertyName, propertyValue);
        return new IllegalUndoEventPayloadPropertyException(msg);
    }
}
