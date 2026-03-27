package aq.project.mappers;

import aq.project.dto.UndoOperationDTO;
import aq.project.entities.UndoOperation;
import aq.project.exceptions.IllegalUndoOperationPayloadPropertyException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class UndoOperationDtoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "personKeycloakId", expression = "java(toPersonKeycloakId(dto))")
    @Mapping(target = "operation", expression = "java(toOperation(dto))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(dto))")
    @Mapping(target = "description", expression = "java(toDescription(dto))")
    public abstract UndoOperation toUndoOperation(UndoOperationDTO dto) throws IllegalUndoOperationPayloadPropertyException;

    @Named("toPersonKeycloakId")
    protected UUID toPersonKeycloakId(UndoOperationDTO dto) throws IllegalUndoOperationPayloadPropertyException {
        if(dto.getPayload().get("person-keycloak-id") == null)
            throw getIllegalUndoOperationPayloadPropertyException("person-keycloak-id", "null");
        return UUID.fromString(dto.getPayload().get("person-keycloak-id"));
    }

    @Named("toOperation")
    protected String toOperation(UndoOperationDTO dto) {
        return dto.getOperation().getValue();
    }

    @Named("toTimestamp")
    protected long toTimestamp(UndoOperationDTO dto) throws IllegalUndoOperationPayloadPropertyException {
        if(dto.getPayload().get("timestamp") == null)
            throw getIllegalUndoOperationPayloadPropertyException("timestamp", "null");
        String created = dto.getPayload().get("timestamp");
        return Long.parseLong(created);
    }

    @Named("toDescription")
    protected String toDescription(UndoOperationDTO dto) throws IllegalUndoOperationPayloadPropertyException {
        if(dto.getPayload().get("description") == null)
            throw getIllegalUndoOperationPayloadPropertyException("description", "null");
        return dto.getPayload().get("description");
    }

    private IllegalUndoOperationPayloadPropertyException getIllegalUndoOperationPayloadPropertyException(String propertyName, String propertyValue) {
        String msg = String.format("Illegal UndoOperation payload property: %s=%s", propertyName, propertyValue);
        return new IllegalUndoOperationPayloadPropertyException(msg);
    }
}
