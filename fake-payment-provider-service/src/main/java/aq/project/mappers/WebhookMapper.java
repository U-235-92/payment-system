package aq.project.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WebhookMapper {

    WebhookMapper INSTANCE = Mappers.getMapper(WebhookMapper.class);
}
