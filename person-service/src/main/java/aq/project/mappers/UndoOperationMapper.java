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
    @Mapping(target = "personKeycloakId", expression = "java(toPersonKeycloakId(dto))")
    @Mapping(target = "operation", expression = "java(toOperation(dto))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(dto))")
    @Mapping(target = "description", expression = "java(toDescription(dto))")
    public abstract UndoEvent toUndoEvent(UndoOperationDTO dto) throws IllegalUndoEventPayloadPropertyException;

    @Named("toPersonKeycloakId")
    protected UUID toPersonKeycloakId(UndoOperationDTO dto) throws IllegalUndoEventPayloadPropertyException {
        if(dto.getPayload().get("person-keycloak-id") == null)
            throw getIllegalUndoEventPayloadPropertyException("person-keycloak-id", "null");
        return UUID.fromString(dto.getPayload().get("person-keycloak-id"));
    }

    @Named("toOperation")
    protected String toOperation(UndoOperationDTO dto) {
        return dto.getOperation().getValue();
    }

    @Named("toTimestamp")
    protected long toTimestamp(UndoOperationDTO dto) throws IllegalUndoEventPayloadPropertyException {
        if(dto.getPayload().get("timestamp") == null)
            throw getIllegalUndoEventPayloadPropertyException("timestamp", "null");
        String created = dto.getPayload().get("timestamp");
        return Long.parseLong(created);
    }

    @Named("toDescription")
    protected String toDescription(UndoOperationDTO dto) throws IllegalUndoEventPayloadPropertyException {
        if(dto.getPayload().get("description") == null)
            throw getIllegalUndoEventPayloadPropertyException("description", "null");
        return dto.getPayload().get("description");
    }

    private IllegalUndoEventPayloadPropertyException getIllegalUndoEventPayloadPropertyException(String propertyName, String propertyValue) {
        String msg = String.format("Illegal UndoEvent payload property: %s=%s", propertyName, propertyValue);
        return new IllegalUndoEventPayloadPropertyException(msg);
    }
}
