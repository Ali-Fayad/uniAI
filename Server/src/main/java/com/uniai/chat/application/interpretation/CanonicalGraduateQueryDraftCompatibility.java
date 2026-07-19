package com.uniai.chat.application.interpretation;

import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;

import java.util.List;

/** Compatibility boundary retained while callers migrate to canonical drafts. */
public final class CanonicalGraduateQueryDraftCompatibility {
    private CanonicalGraduateQueryDraftCompatibility() {}

    public static GraduateQueryInterpretation toLegacyInterpretation(CanonicalGraduateQueryDraft draft) {
        GraduateKnowledgeResource resource = GraduateKnowledgeResource.valueOf(draft.resource());
        GraduateKnowledgeOperation operation = GraduateKnowledgeOperation.valueOf(draft.operation());
        String intent = GraduateKnowledgeQuery.deriveIntent(resource, operation).name();
        CanonicalGraduateQueryDraft.Filters filters = draft.filters();
        CanonicalGraduateQueryDraft.Tuition tuition = filters.tuition();
        return new GraduateQueryInterpretation(
                draft.schemaVersion(), intent, filters.universities(), filters.degreeTypes(), draft.detailLevel(),
                false, draft.comparison() != null, filters.topicKeywords(), draft.clarificationRequired(), null,
                draft.unsupportedConstraints(), draft.resource(), draft.operation(), filters.city(), filters.faculty(),
                filters.department(), filters.languages(), filters.admissionRequirementTypes(), filters.programName(),
                draft.aggregation() == null ? null : draft.aggregation().function(),
                tuition == null ? null : tuition.thresholdOperator(), tuition == null ? null : tuition.thresholdValue(),
                tuition == null ? null : tuition.currency(), tuition == null ? null : tuition.billingBasis(),
                tuition == null ? null : tuition.academicYear(), tuition == null ? null : tuition.scopeLevel(),
                draft.sort() == null ? null : draft.sort().field(), draft.sort() == null ? null : draft.sort().direction(),
                draft.limit(), draft.comparison() == null ? null : draft.comparison().dimension()
        );
    }

    public static CanonicalGraduateQueryDraft fromLegacyInterpretation(GraduateQueryInterpretation interpretation) {
        if (interpretation == null) return null;
        CanonicalGraduateQueryDraft.Filters filters = new CanonicalGraduateQueryDraft.Filters(
                interpretation.universities(), interpretation.degreeTypes(), interpretation.city(),
                interpretation.faculty(), interpretation.department(), interpretation.programName(),
                interpretation.languages(), interpretation.admissionRequirementTypes(), interpretation.topicKeywords(),
                new CanonicalGraduateQueryDraft.Tuition(
                        interpretation.thresholdOperator(), interpretation.thresholdValue(), interpretation.currency(),
                        interpretation.billingBasis(), interpretation.academicYear(), interpretation.tuitionScopeLevel())
        );
        String inferredIntent = intent(interpretation).name();
        String aggregationFunction = interpretation.aggregation();
        CanonicalGraduateQueryDraft.Aggregation aggregation = aggregationFunction == null
                && "TUITION_AGGREGATION".equals(inferredIntent)
                ? new CanonicalGraduateQueryDraft.Aggregation("AVG", "TUITION")
                : aggregationFunction == null
                ? null
                : new CanonicalGraduateQueryDraft.Aggregation(aggregationFunction, "TUITION");
        CanonicalGraduateQueryDraft.Sort sort = interpretation.sortField() == null
                ? null
                : new CanonicalGraduateQueryDraft.Sort(interpretation.sortField(), interpretation.sortDirection());
        CanonicalGraduateQueryDraft.Comparison comparison = interpretation.comparisonDimension() == null
                ? null
                : new CanonicalGraduateQueryDraft.Comparison(interpretation.comparisonDimension());
        String resource = interpretation.resource();
        String operation = interpretation.operation();
        if (resource == null || resource.isBlank()) resource = GraduateKnowledgeQuery.resourceFor(intent(interpretation)).name();
        if (operation == null || operation.isBlank()) operation = GraduateKnowledgeQuery.operationFor(intent(interpretation), detailLevel(interpretation)).name();
        String detailLevel = interpretation.detailLevel();
        if (detailLevel != null
                && !detailLevel.equalsIgnoreCase("LIST")
                && !detailLevel.equalsIgnoreCase("DETAILS")) {
            detailLevel = null;
        }
        return new CanonicalGraduateQueryDraft(
                2, resource, operation, filters, aggregation, sort, comparison,
                detailLevel, interpretation.limit(), interpretation.ambiguous(),
                interpretation.unsupportedConstraints());
    }

    private static com.uniai.chat.application.retrieval.GraduateKnowledgeIntent intent(GraduateQueryInterpretation interpretation) {
        try {
            return com.uniai.chat.application.retrieval.GraduateKnowledgeIntent.valueOf(interpretation.intent().trim().toUpperCase());
        } catch (Exception ignored) {
            return com.uniai.chat.application.retrieval.GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
        }
    }

    private static com.uniai.chat.application.retrieval.GraduateProgramDetailLevel detailLevel(GraduateQueryInterpretation interpretation) {
        try {
            return interpretation.detailLevel() == null
                    ? com.uniai.chat.application.retrieval.GraduateProgramDetailLevel.LIST
                    : com.uniai.chat.application.retrieval.GraduateProgramDetailLevel.valueOf(interpretation.detailLevel().trim().toUpperCase());
        } catch (Exception ignored) {
            return com.uniai.chat.application.retrieval.GraduateProgramDetailLevel.LIST;
        }
    }
}
