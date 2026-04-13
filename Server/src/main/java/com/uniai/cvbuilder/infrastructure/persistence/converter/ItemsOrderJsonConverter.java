package com.uniai.cvbuilder.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.cvbuilder.domain.model.ItemsOrder;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ItemsOrderJsonConverter implements AttributeConverter<ItemsOrder, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ItemsOrder attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting ItemsOrder to JSON", e);
        }
    }

    @Override
    public ItemsOrder convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ItemsOrder();
        }
        try {
            return objectMapper.readValue(dbData, ItemsOrder.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to ItemsOrder", e);
        }
    }
}
