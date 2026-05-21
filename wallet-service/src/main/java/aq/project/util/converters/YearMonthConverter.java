package aq.project.util.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    @Override
    public String convertToDatabaseColumn(YearMonth yearMonth) {
        if(yearMonth == null)
            return null;
        return yearMonth.format(YEAR_MONTH_FORMATTER);
    }

    @Override
    public YearMonth convertToEntityAttribute(String s) {
        if(s == null)
            return null;
        return YearMonth.parse(s, YEAR_MONTH_FORMATTER);
    }
}
