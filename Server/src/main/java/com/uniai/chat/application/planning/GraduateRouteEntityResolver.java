package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.chat.application.retrieval.GraduateKnowledgeEntityResolutionResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeEntityResolutionStatus;
import com.uniai.chat.application.retrieval.GraduateKnowledgeEntityResolver;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

/** Resolves planner aliases to canonical server-owned university identities. */
public final class GraduateRouteEntityResolver {
    private final GraduateKnowledgeEntityResolver universityResolver;

    public GraduateRouteEntityResolver() {
        this(new GraduateKnowledgeEntityResolver());
    }

    public GraduateRouteEntityResolver(GraduateKnowledgeEntityResolver universityResolver) {
        this.universityResolver = universityResolver;
    }

    public <T> ResolvedGraduateRoutePlan<T> resolve(ValidatedGraduateRoutePlan<T> plan,
                                                    List<UniversityCatalog> catalogs,
                                                    String currentUserMessage) {
        if (plan.route() == GraduateAiRoute.DIRECT_AI_RESPONSE) {
            return new ResolvedGraduateRoutePlan<>(plan.route(), plan.arguments(),
                    plan.canonicalArguments(), List.of());
        }
        List<String> references = universityReferences(plan.arguments());
        GraduateKnowledgeEntityResolutionResult result = universityResolver.resolve(
                references, catalogs, currentUserMessage);
        if (result.status() == GraduateKnowledgeEntityResolutionStatus.UNKNOWN) {
            throw new GraduateRoutePlanningException("Unknown university reference");
        }
        if (result.status() == GraduateKnowledgeEntityResolutionStatus.AMBIGUOUS) {
            throw new GraduateRoutePlanningException("Ambiguous university reference");
        }
        return new ResolvedGraduateRoutePlan<>(plan.route(), plan.arguments(),
                canonicalize(plan.canonicalArguments(), result.universities()), result.universities());
    }

    private List<String> universityReferences(Object arguments) {
        List<String> references = new ArrayList<>();
        for (RecordComponent component : arguments.getClass().getRecordComponents()) {
            if (!component.getName().equals("university") && !component.getName().equals("universities")) continue;
            try {
                Object value = component.getAccessor().invoke(arguments);
                if (value instanceof String text && !text.isBlank()) references.add(text);
                if (value instanceof List<?> values) values.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .filter(text -> !text.isBlank())
                        .forEach(references::add);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new GraduateRoutePlanningException("Unable to inspect route university arguments", ex);
            }
        }
        return references;
    }

    private ObjectNode canonicalize(com.fasterxml.jackson.databind.JsonNode arguments,
                                    List<ResolvedUniversity> universities) {
        ObjectNode canonical = ((ObjectNode) arguments).deepCopy();
        if (universities.isEmpty()) return canonical;
        if (canonical.has("university")) {
            canonical.put("university", universities.get(0).name());
        }
        if (canonical.has("universities")) {
            ArrayNode values = canonical.putArray("universities");
            universities.forEach(university -> values.add(university.name()));
        }
        return canonical;
    }
}
