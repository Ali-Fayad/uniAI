package com.uniai.cvbuilder.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.cvbuilder.domain.model.SelectedItems;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SelectedItemsJsonConverter implements AttributeConverter<SelectedItems, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(SelectedItems attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting SelectedItems to JSON", e);
        }
    }

    @Override
    public SelectedItems convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new SelectedItems();
        }
        try {
            return objectMapper.readValue(dbData, SelectedItems.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to SelectedItems", e);
        }
    }
}
