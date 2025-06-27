package com.example.shapemanegement.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.data.repository.query.Param;
@Converter(autoApply = false)
public class StringToJsonConverter implements AttributeConverter<String,String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        // Escape any existing double quotes within the string to prevent JSON parsing issues
        String escapedAttribute = attribute.replace("\"", "\\\"");
        // Wrap the entire string in double quotes to make it a valid JSON string literal
        return "\"" + escapedAttribute + "\"";
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Check if the string starts and ends with a quote, indicating it was stored as a JSON string literal
        if (dbData.length() >= 2 && dbData.startsWith("\"") && dbData.endsWith("\"")) {
            // Remove the outer quotes
            dbData = dbData.substring(1, dbData.length() - 1);
        }
        // Unescape any escaped double quotes within the string
        return dbData.replace("\\\"", "\"");


}}
