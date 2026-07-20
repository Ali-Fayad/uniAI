package com.uniai.chat.infrastructure.prompt;

import com.uniai.chat.application.planning.GraduateAiRouteCatalog;
import com.uniai.chat.application.planning.GraduateAiRouteDefinition;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPromptPort;
import com.uniai.chat.infrastructure.config.GraduateQueryInterpretationProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Loads planner rules and generates the route/argument catalog from Java metadata. */
public final class GraduateRoutePlannerPromptProvider implements GraduateRoutePlannerPromptPort {
    private static final Logger logger = LogManager.getLogger(GraduateRoutePlannerPromptProvider.class);
    private static final String CATALOG_PLACEHOLDER = "{{ROUTE_CATALOG}}";
    private final String prompt;

    public GraduateRoutePlannerPromptProvider(GraduateQueryInterpretationProperties properties,
                                              GraduateAiRouteCatalog catalog) {
        String path = properties != null && properties.getRoutePlannerPromptPath() != null
                && !properties.getRoutePlannerPromptPath().isBlank()
                ? properties.getRoutePlannerPromptPath().trim()
                : "prompts/graduate-route-planner-prompt.txt";
        String template = load(path);
        if (!template.contains(CATALOG_PLACEHOLDER)) {
            throw new IllegalStateException("Graduate route planner prompt is missing " + CATALOG_PLACEHOLDER);
        }
        prompt = template.replace(CATALOG_PLACEHOLDER, renderCatalog(catalog));
        logger.info("[PROMPT] Graduate route planner prompt loaded path={} routeCount={} size={}",
                path, catalog.definitions().size(), prompt.length());
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    private String load(String path) {
        try {
            return new String(new ClassPathResource(path).getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load graduate route planner prompt from " + path, ex);
        }
    }

    private String renderCatalog(GraduateAiRouteCatalog catalog) {
        return catalog.definitions().values().stream()
                .filter(GraduateAiRouteDefinition::enabled)
                .map(this::renderRoute)
                .collect(Collectors.joining("\n"));
    }

    private String renderRoute(GraduateAiRouteDefinition<?> definition) {
        Set<String> required = Set.copyOf(definition.requiredArguments());
        String arguments = Arrays.stream(definition.argumentType().getRecordComponents())
                .map(component -> renderArgument(component, required.contains(component.getName())))
                .collect(Collectors.joining(", "));
        return definition.route() + "|" + definition.purpose()
                + "|args{" + arguments + "}"
                + (definition.maximumResultLimit() > 0 ? "|max=" + definition.maximumResultLimit() : "");
    }

    private String renderArgument(RecordComponent component, boolean required) {
        return component.getName() + ":" + typeName(component.getGenericType()) + (required ? "!" : "?");
    }

    private String typeName(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            return "array<" + typeName(parameterizedType.getActualTypeArguments()[0]) + ">";
        }
        if (type instanceof Class<?> typeClass && typeClass.isEnum()) {
            return Arrays.stream(typeClass.getEnumConstants()).map(Object::toString).collect(Collectors.joining("|", "enum(", ")"));
        }
        if (type == String.class) return "string";
        if (type == Integer.class) return "integer";
        if (type == Boolean.class) return "boolean";
        return type.getTypeName();
    }
}
