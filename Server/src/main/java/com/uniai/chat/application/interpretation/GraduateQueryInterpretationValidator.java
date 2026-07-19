package com.uniai.chat.application.interpretation;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.chat.application.retrieval.GraduateKnowledgeEntityResolutionResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeEntityResolutionStatus;
import com.uniai.chat.application.retrieval.GraduateKnowledgeEntityResolver;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQueryFactory;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;
import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Compatibility entry point and final executable-query safety boundary.
 * Entity identity and query construction are delegated to dedicated services.
 */
public class GraduateQueryInterpretationValidator {
    private static final int MAX_DEGREE_TYPES = 4;
    private static final int MAX_UNSUPPORTED_CONSTRAINTS = 5;
    private static final Set<String> SUPPORTED_ADMISSION_TYPES = Set.of(
            "GENERAL", "GRE", "GMAT", "ENGLISH", "PORTFOLIO", "INTERVIEW",
            "EXPERIENCE", "ACADEMIC", "PREREQUISITE", "OTHER"
    );

    private final GraduateKnowledgeEntityResolver entityResolver;
    private final GraduateKnowledgeQueryFactory queryFactory;
    private final CanonicalGraduateQueryDraftValidator draftValidator;

    public GraduateQueryInterpretationValidator() {
        this(new GraduateKnowledgeEntityResolver(), new GraduateKnowledgeQueryFactory());
    }

    public GraduateQueryInterpretationValidator(
            GraduateKnowledgeEntityResolver entityResolver,
            GraduateKnowledgeQueryFactory queryFactory
    ) {
        this.entityResolver = entityResolver;
        this.queryFactory = queryFactory;
        this.draftValidator = new CanonicalGraduateQueryDraftValidator();
    }

    public GraduateQueryInterpretationResult validate(
            GraduateQueryInterpretation interpretation,
            List<UniversityCatalog> catalogs
    ) {
        return validate(interpretation, catalogs, null);
    }

    public GraduateQueryInterpretationResult validate(
            GraduateQueryInterpretation interpretation,
            List<UniversityCatalog> catalogs,
            String currentUserMessage
    ) {
        if (interpretation == null) return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_EMPTY");
        if (interpretation.schemaVersion() == null
                || (interpretation.schemaVersion() != 1 && interpretation.schemaVersion() != 2)) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_SCHEMA_VERSION_UNSUPPORTED");
        }

        GraduateKnowledgeIntent compatibilityIntent = parseIntent(interpretation.intent());
        if (compatibilityIntent == null) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_INVALID_INTENT");
        }
        CanonicalGraduateQueryDraft draft = CanonicalGraduateQueryDraftCompatibility.fromLegacyInterpretation(interpretation);
        GraduateQueryInterpretationResult result = validateCanonicalDraft(
                draft, catalogs, currentUserMessage, Boolean.TRUE.equals(interpretation.followUp()), compatibilityIntent);
        if (result.status() == GraduateQueryInterpretationStatus.INVALID
                && "AI_QUERY_INTERPRETATION_INVALID_DRAFT".equals(result.failureCategory())) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_RESOURCE_OPERATION_UNSUPPORTED");
        }
        return result;
    }

    /** Primary Task 4 orchestration entry point for provider output. */
    public GraduateQueryInterpretationResult validateCanonicalDraft(
            CanonicalGraduateQueryDraft draft,
            List<UniversityCatalog> catalogs,
            String currentUserMessage,
            boolean followUpResolved
    ) {
        return validateCanonicalDraft(draft, catalogs, currentUserMessage, followUpResolved, null);
    }

    private GraduateQueryInterpretationResult validateCanonicalDraft(
            CanonicalGraduateQueryDraft draft,
            List<UniversityCatalog> catalogs,
            String currentUserMessage,
            boolean followUpResolved,
            GraduateKnowledgeIntent compatibilityIntent
    ) {
        if (draft == null) return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_EMPTY");
        try {
            draftValidator.validate(draft);
        } catch (IllegalArgumentException ex) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_INVALID_DRAFT");
        }

        List<String> degrees = normalizeList(draft.filters().degreeTypes(), MAX_DEGREE_TYPES);
        List<String> unsupported = normalizeList(draft.unsupportedConstraints(), MAX_UNSUPPORTED_CONSTRAINTS);
        if (degrees == null || unsupported == null) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_TOO_LARGE");
        }
        if (containsUnsupportedDegree(degrees) || containsUnsupportedConstraint(unsupported)) {
            return GraduateQueryInterpretationResult.unsupported(
                    "I can help with master's and PhD graduate questions only.",
                    0,
                    countSupportedDegrees(degrees),
                    mergeUnsupportedConstraints(degrees, unsupported)
            );
        }
        if (draft.filters().admissionRequirementTypes().stream()
                .filter(this::hasText)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(value -> !SUPPORTED_ADMISSION_TYPES.contains(value))) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_ADMISSION_TYPE_UNSUPPORTED");
        }

        GraduateKnowledgeEntityResolutionResult resolution = entityResolver.resolve(
                draft.filters().universities(), catalogs, currentUserMessage);
        GraduateKnowledgeIntent intent = compatibilityIntent == null
                ? (draft.resource().equals("NONE") && draft.operation().equals("NONE")
                ? GraduateKnowledgeIntent.GENERAL_CHAT
                : GraduateKnowledgeQuery.deriveIntent(
                GraduateKnowledgeResource.valueOf(draft.resource()),
                GraduateKnowledgeOperation.valueOf(draft.operation())))
                : compatibilityIntent;
        GraduateKnowledgeQuery query;
        try {
            query = queryFactory.create(draft, resolution.universities(), intent, followUpResolved);
        } catch (IllegalArgumentException ex) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_RESOURCE_OPERATION_UNSUPPORTED");
        }

        if (draft.clarificationRequired()) {
            return GraduateQueryInterpretationResult.ambiguous(
                    buildAmbiguousMessage(intent), resolution.universities().size(), countSupportedDegrees(degrees),
                    query.withDecisionMetadata(
                            com.uniai.chat.application.retrieval.GraduateKnowledgeInterpretationSource.AI,
                            com.uniai.chat.application.retrieval.GraduateKnowledgeAmbiguityReason.MISSING_REQUIRED_SCOPE,
                            true));
        }

        if (resolution.status() == GraduateKnowledgeEntityResolutionStatus.UNKNOWN
                || resolution.status() == GraduateKnowledgeEntityResolutionStatus.AMBIGUOUS) {
            return GraduateQueryInterpretationResult.ambiguous(
                    buildAmbiguousMessage(intent),
                    resolution.universities().size(),
                    countSupportedDegrees(degrees),
                    query.withDecisionMetadata(
                            com.uniai.chat.application.retrieval.GraduateKnowledgeInterpretationSource.AI,
                            com.uniai.chat.application.retrieval.GraduateKnowledgeAmbiguityReason.UNRESOLVED_UNIVERSITY,
                            true)).withOutcome(resolution.status() == GraduateKnowledgeEntityResolutionStatus.UNKNOWN
                    ? GraduateQueryInterpretationOutcome.UNRESOLVED_ENTITY
                    : GraduateQueryInterpretationOutcome.AMBIGUOUS_ENTITY);
        }

        if (requiresUniversity(intent)
                && resolution.universities().isEmpty()
                && query.filters().city() == null) {
            return GraduateQueryInterpretationResult.ambiguous(
                    buildAmbiguousMessage(intent),
                    0,
                    countSupportedDegrees(degrees),
                    query.withDecisionMetadata(
                            com.uniai.chat.application.retrieval.GraduateKnowledgeInterpretationSource.AI,
                            com.uniai.chat.application.retrieval.GraduateKnowledgeAmbiguityReason.MISSING_REQUIRED_SCOPE,
                            true));
        }

        if (intent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS
                && query.resource() == GraduateKnowledgeResource.NONE
                && query.operation() == GraduateKnowledgeOperation.NONE) {
            return GraduateQueryInterpretationResult.ambiguous(
                    buildAmbiguousMessage(intent),
                    resolution.universities().size(),
                    countSupportedDegrees(degrees),
                    query.withDecisionMetadata(
                            com.uniai.chat.application.retrieval.GraduateKnowledgeInterpretationSource.AI,
                            com.uniai.chat.application.retrieval.GraduateKnowledgeAmbiguityReason.MISSING_REQUIRED_SCOPE,
                            true));
        }

        return validateExecutableQuery(query, resolution, degrees);
    }

    /** Final safety validation for already-constructed executable queries. */
    public GraduateQueryInterpretationResult validateExecutableQuery(
            GraduateKnowledgeQuery query,
            GraduateKnowledgeEntityResolutionResult resolution,
            List<String> degreeTypes
    ) {
        if (query == null) return GraduateQueryInterpretationResult.invalid("AI_QUERY_EXECUTABLE_QUERY_EMPTY");
        if (!GraduateKnowledgeQuery.isCompatible(query.resource(), query.operation())) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_EXECUTABLE_RESOURCE_OPERATION_UNSUPPORTED");
        }
        return GraduateQueryInterpretationResult.valid(
                query,
                resolution == null ? query.resolvedUniversities().size() : resolution.universities().size(),
                degreeTypes == null ? query.degreeTypes().size() : degreeTypes.size());
    }

    private GraduateKnowledgeIntent parseIntent(String value) {
        if (!hasText(value)) return null;
        try {
            return GraduateKnowledgeIntent.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private List<String> normalizeList(List<String> values, int maxSize) {
        if (values == null || values.size() > maxSize) return null;
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (!hasText(value)) continue;
            if (value.trim().length() > 120) return null;
            normalized.add(value.trim());
        }
        return List.copyOf(normalized);
    }

    private boolean containsUnsupportedDegree(List<String> values) {
        return values.stream().map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(value -> value.equals("BACHELOR") || value.equals("UNDERGRADUATE") || value.equals("UNDERGRAD"));
    }

    private boolean containsUnsupportedConstraint(List<String> values) {
        return values.stream().map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(value -> value.contains("BACHELOR") || value.contains("UNDERGRADUATE") || value.contains("UNDERGRAD"));
    }

    private List<String> mergeUnsupportedConstraints(List<String> degrees, List<String> unsupported) {
        Set<String> values = new LinkedHashSet<>();
        values.addAll(degrees);
        values.addAll(unsupported);
        return values.stream().limit(MAX_UNSUPPORTED_CONSTRAINTS).toList();
    }

    private int countSupportedDegrees(List<String> values) {
        return (int) values.stream()
                .filter(value -> value.equalsIgnoreCase("MASTER") || value.equalsIgnoreCase("PHD"))
                .count();
    }

    private boolean requiresUniversity(GraduateKnowledgeIntent intent) {
        return intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP
                || intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                || intent == GraduateKnowledgeIntent.GRADUATE_OVERVIEW
                || intent == GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP;
    }

    private String buildAmbiguousMessage(GraduateKnowledgeIntent intent) {
        return intent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS
                ? "I need a clearer university or graduate query to answer safely."
                : "I need a clearer university reference to answer safely.";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
