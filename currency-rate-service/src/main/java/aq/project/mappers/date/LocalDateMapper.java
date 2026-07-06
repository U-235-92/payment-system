package aq.project.mappers.date;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static aq.project.util.constants.CustomConstants.ISO_DATE_FORMAT;

@Converter(autoApply = true)
@RequiredArgsConstructor
public class LocalDateMapper implements AttributeConverter<LocalDate, String> {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT);

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        return attribute.format(dateTimeFormatter);
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        return LocalDate.from(dateTimeFormatter.parse(dbData));
    }
}
