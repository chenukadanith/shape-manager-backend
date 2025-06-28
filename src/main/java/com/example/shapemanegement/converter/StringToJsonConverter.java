package com.example.shapemanegement.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.data.repository.query.Param;
@Converter(autoApply = false)
public class StringToJsonConverter implements AttributeConverter<String,String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        //nullable check
        if (attribute == null) {
            return null;
        }
        String escapedAttribute = attribute.replace("\"", "\\\"");

        return "\"" + escapedAttribute + "\"";
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Check if the string starts and ends with a quote, indicating it was stored as a JSON string literal
        if (dbData.length() >= 2 && dbData.startsWith("\"") && dbData.endsWith("\"")) {

            dbData = dbData.substring(1, dbData.length() - 1);
        }
        // Unescape any escaped double quotes within the string
        return dbData.replace("\\\"", "\"");


}}
