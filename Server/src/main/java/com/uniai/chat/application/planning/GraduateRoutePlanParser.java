package com.uniai.chat.application.planning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Strictly parses the two-field planner envelope and a route-specific argument record. */
public final class GraduateRoutePlanParser {
    private static final Set<String> TOP_LEVEL_FIELDS = Set.of("route", "arguments");
    private final GraduateAiRouteCatalog catalog;
    private final ObjectMapper objectMapper;

    public GraduateRoutePlanParser(GraduateAiRouteCatalog catalog, ObjectMapper objectMapper) {
        this.catalog = catalog;
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, false)
                .configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false);
    }

    public ValidatedGraduateRoutePlan<?> parse(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            throw invalid("PLANNER_RESPONSE_EMPTY");
        }
        final JsonNode root;
        try {
            root = objectMapper.readTree(rawJson);
        } catch (JsonProcessingException ex) {
            throw new GraduateRoutePlanningException("Invalid graduate route plan: MALFORMED_JSON", ex);
        }
        if (!root.isObject()) throw invalid("TOP_LEVEL_OBJECT_REQUIRED");
        rejectUnknownFields(root, TOP_LEVEL_FIELDS, "TOP_LEVEL_FIELD_UNKNOWN");
        if (root.size() != TOP_LEVEL_FIELDS.size()) throw invalid("TOP_LEVEL_FIELDS_REQUIRED");

        JsonNode routeNode = root.get("route");
        JsonNode argumentsNode = root.get("arguments");
        if (routeNode == null || !routeNode.isTextual() || routeNode.textValue().isBlank()) {
            throw invalid("ROUTE_REQUIRED");
        }
        if (argumentsNode == null || !argumentsNode.isObject()) {
            throw invalid("ARGUMENTS_OBJECT_REQUIRED");
        }

        final GraduateAiRoute route;
        try {
            route = GraduateAiRoute.valueOf(routeNode.textValue().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw invalid("ROUTE_UNKNOWN");
        }
        GraduateAiRouteDefinition<?> definition = catalog.definition(route);
        if (!definition.enabled()) throw invalid("ROUTE_DISABLED");
        return deserialize(definition, argumentsNode);
    }

    /**
     * Repairs only the safe, text-derived required query argument. All other
     * contract failures remain rejected by the normal strict parser.
     */
    public ParseResult parseWithQueryRepair(String rawJson, String fallbackQuery) {
        try {
            return new ParseResult(parse(rawJson), false, null);
        } catch (GraduateRoutePlanningException original) {
            if (!original.getMessage().contains("ARGUMENT_REQUIRED_QUERY")
                    || fallbackQuery == null || fallbackQuery.isBlank()) {
                throw original;
            }
            final JsonNode root;
            try {
                root = objectMapper.readTree(rawJson);
            } catch (JsonProcessingException ex) {
                throw original;
            }
            if (!root.isObject() || root.get("route") == null || !root.get("route").isTextual()
                    || root.get("arguments") == null || !root.get("arguments").isObject()) {
                throw original;
            }
            final GraduateAiRoute route;
            try {
                route = GraduateAiRoute.valueOf(root.get("route").textValue().trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw original;
            }
            GraduateAiRouteDefinition<?> definition;
            try {
                definition = catalog.definition(route);
            } catch (GraduateRoutePlanningException ex) {
                throw original;
            }
            if (!definition.requiredArguments().contains("query")) throw original;

            ObjectNode repairedRoot = (ObjectNode) root.deepCopy();
            ObjectNode repairedArguments = (ObjectNode) repairedRoot.get("arguments");
            JsonNode query = repairedArguments.get("query");
            if (query != null && !query.isNull() && (!query.isTextual() || !query.textValue().isBlank())) {
                throw original;
            }
            repairedArguments.put("query", fallbackQuery.trim());
            try {
                ValidatedGraduateRoutePlan<?> repaired = parse(objectMapper.writeValueAsString(repairedRoot));
                return new ParseResult(repaired, true, "query");
            } catch (JsonProcessingException | GraduateRoutePlanningException ex) {
                throw original;
            }
        }
    }

    public record ParseResult(ValidatedGraduateRoutePlan<?> plan, boolean repaired, String repairedArgument) {}

    private <T> ValidatedGraduateRoutePlan<T> deserialize(GraduateAiRouteDefinition<T> definition, JsonNode argumentsNode) {
        Set<String> allowedFields = new HashSet<>();
        HashMap<String, RecordComponent> components = new HashMap<>();
        for (RecordComponent component : definition.argumentType().getRecordComponents()) {
            allowedFields.add(component.getName());
            components.put(component.getName(), component);
        }
        rejectUnknownFields(argumentsNode, allowedFields, "ARGUMENT_UNKNOWN");
        for (String required : definition.requiredArguments()) {
            JsonNode value = argumentsNode.get(required);
            if (value == null || value.isNull() || (value.isTextual() && value.textValue().isBlank())
                    || (value.isArray() && value.isEmpty())) {
                throw invalid("ARGUMENT_REQUIRED_" + required.toUpperCase(Locale.ROOT));
            }
        }
        argumentsNode.fields().forEachRemaining(entry -> {
            if (!entry.getValue().isNull()) validateJsonType(entry.getValue(), components.get(entry.getKey()));
        });

        final T arguments;
        try {
            arguments = objectMapper.treeToValue(argumentsNode, definition.argumentType());
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new GraduateRoutePlanningException("Invalid graduate route plan: ARGUMENT_TYPE_INVALID", ex);
        }
        validateLimit(definition, argumentsNode);
        return new ValidatedGraduateRoutePlan<>(definition.route(), arguments, argumentsNode.deepCopy());
    }

    private void validateJsonType(JsonNode value, RecordComponent component) {
        Class<?> type = component.getType();
        boolean valid;
        if (type == String.class || type.isEnum()) {
            valid = value.isTextual();
        } else if (type == Integer.class) {
            valid = value.isIntegralNumber() && value.canConvertToInt();
        } else if (type == Boolean.class) {
            valid = value.isBoolean();
        } else if (List.class.isAssignableFrom(type)) {
            valid = value.isArray();
            if (valid) validateArrayElements(value, component.getGenericType());
        } else {
            valid = value.isObject();
        }
        if (!valid) throw invalid("ARGUMENT_TYPE_INVALID_" + component.getName().toUpperCase(Locale.ROOT));
    }

    private void validateArrayElements(JsonNode value, Type genericType) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) return;
        Type elementType = parameterizedType.getActualTypeArguments()[0];
        for (JsonNode element : value) {
            if (element.isNull()) throw invalid("ARGUMENT_ARRAY_ELEMENT_NULL");
            if (elementType == String.class && !element.isTextual()) throw invalid("ARGUMENT_ARRAY_ELEMENT_TYPE_INVALID");
            if (elementType instanceof Class<?> elementClass && elementClass.isEnum() && !element.isTextual()) {
                throw invalid("ARGUMENT_ARRAY_ELEMENT_TYPE_INVALID");
            }
        }
    }

    private void validateLimit(GraduateAiRouteDefinition<?> definition, JsonNode argumentsNode) {
        JsonNode limit = argumentsNode.get("limit");
        if (limit == null || limit.isNull()) return;
        if (!limit.isIntegralNumber() || !limit.canConvertToInt()) throw invalid("LIMIT_TYPE_INVALID");
        int value = limit.intValue();
        if (value < 1 || value > definition.maximumResultLimit()) {
            throw invalid("LIMIT_OUT_OF_RANGE");
        }
    }

    private void rejectUnknownFields(JsonNode object, Set<String> allowed, String reason) {
        Iterator<String> names = object.fieldNames();
        while (names.hasNext()) {
            if (!allowed.contains(names.next())) throw invalid(reason);
        }
    }

    private GraduateRoutePlanningException invalid(String reason) {
        return new GraduateRoutePlanningException("Invalid graduate route plan: " + reason);
    }
}
