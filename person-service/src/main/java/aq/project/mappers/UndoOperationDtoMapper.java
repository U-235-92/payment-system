package aq.project.mappers;

import aq.project.dto.UndoOperationDto;
import aq.project.entities.UndoOperation;
import aq.project.exceptions.IllegalUndoOperationPayloadPropertyException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.UUID;

import static aq.project.utils.constants.CustomConstants.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class UndoOperationDtoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "personKeycloakId", expression = "java(toPersonKeycloakId(dto))")
    @Mapping(target = "operation", expression = "java(toOperation(dto))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(dto))")
    @Mapping(target = "description", expression = "java(toDescription(dto))")
    public abstract UndoOperation toUndoOperation(UndoOperationDto dto) throws IllegalUndoOperationPayloadPropertyException;

    @Named("toPersonKeycloakId")
    protected UUID toPersonKeycloakId(UndoOperationDto dto) throws IllegalUndoOperationPayloadPropertyException {
        if(dto.getPayload().get(UNDO_OPERATION_PERSON_ID) == null)
            throw getIllegalUndoOperationPayloadPropertyException(UNDO_OPERATION_PERSON_ID, "null");
        return UUID.fromString(dto.getPayload().get(UNDO_OPERATION_PERSON_ID));
    }

    @Named("toOperation")
    protected String toOperation(UndoOperationDto dto) {
        return dto.getOperation().getValue();
    }

    @Named("toTimestamp")
    protected long toTimestamp(UndoOperationDto dto) throws IllegalUndoOperationPayloadPropertyException {
        if(dto.getPayload().get(UNDO_OPERATION_TIMESTAMP) == null)
            throw getIllegalUndoOperationPayloadPropertyException(UNDO_OPERATION_TIMESTAMP, "null");
        String created = dto.getPayload().get(UNDO_OPERATION_TIMESTAMP);
        return Long.parseLong(created);
    }

    @Named("toDescription")
    protected String toDescription(UndoOperationDto dto) throws IllegalUndoOperationPayloadPropertyException {
        if(dto.getPayload().get(UNDO_OPERATION_DESCRIPTION) == null)
            throw getIllegalUndoOperationPayloadPropertyException(UNDO_OPERATION_DESCRIPTION, "null");
        return dto.getPayload().get(UNDO_OPERATION_DESCRIPTION);
    }

    private IllegalUndoOperationPayloadPropertyException getIllegalUndoOperationPayloadPropertyException(String propertyName, String propertyValue) {
        String msg = String.format("Illegal UndoOperation payload property: %s=%s", propertyName, propertyValue);
        return new IllegalUndoOperationPayloadPropertyException(msg);
    }
}
