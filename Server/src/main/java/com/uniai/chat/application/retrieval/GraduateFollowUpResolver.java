package com.uniai.chat.application.retrieval;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.MemoryUniversityRef;
import com.uniai.catalog.domain.model.UniversityCatalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GraduateFollowUpResolver {

    public GraduateFollowUpResolutionResult resolve(
            String currentUserMessage,
            GraduateKnowledgeQuery candidateQuery,
            List<AiConversationMessage> recentConversationHistory,
            ConversationMemory conversationMemory,
            List<UniversityCatalog> universityCatalogs
    ) {
        if (candidateQuery == null) {
            return GraduateFollowUpResolutionResult.clarificationRequired("NO_CANDIDATE_QUERY", null, List.of("current message"));
        }

        if (candidateQuery.intent() == GraduateKnowledgeIntent.GENERAL_CHAT) {
            return GraduateFollowUpResolutionResult.unchanged(candidateQuery, List.of("current message"));
        }

        if (candidateQuery.intent() == GraduateKnowledgeIntent.LOCATION_LOOKUP) {
            List<UniversityCatalog> locationCatalogs = universityCatalogs == null ? List.of() : List.copyOf(universityCatalogs);
            List<ResolvedUniversity> explicitLocationUniversities = GraduateKnowledgeResolutionSupport.resolveUniversities(
                    normalize(currentUserMessage), locationCatalogs);
            if (!explicitLocationUniversities.isEmpty()) {
                GraduateKnowledgeQuery replaced = new GraduateKnowledgeQuery(
                        candidateQuery.intent(), candidateQuery.resource(), candidateQuery.operation(),
                        new GraduateKnowledgeFilters(
                                explicitLocationUniversities,
                                candidateQuery.degreeTypes(),
                                candidateQuery.topicKeywords(),
                                candidateQuery.filters().city()
                        ), candidateQuery.aggregation(), candidateQuery.sort(), candidateQuery.limit(),
                        candidateQuery.followUpContext(), candidateQuery.detailLevel(), true, false)
                        .withDecisionMetadata(GraduateKnowledgeInterpretationSource.FOLLOW_UP, GraduateKnowledgeAmbiguityReason.NONE, false);
                return GraduateFollowUpResolutionResult.resolved(replaced, List.of("current message"));
            }
            List<ResolvedUniversity> comparisonScope = comparisonUniversities(conversationMemory);
            if (candidateQuery.operation() == GraduateKnowledgeOperation.COMPARE && comparisonScope.size() > 1) {
                GraduateKnowledgeFollowUpContext context = new GraduateKnowledgeFollowUpContext(
                        null, null, candidateQuery.resource(), candidateQuery.operation(),
                        comparisonScope.stream().map((university) -> new GraduateKnowledgeReference(
                                GraduateKnowledgeReferenceKind.UNIVERSITY,
                                university.name(), university.acronym(), comparisonScope.indexOf(university) + 1)).toList(),
                        candidateQuery.followUpContext().comparisonDimension());
                GraduateKnowledgeQuery compared = new GraduateKnowledgeQuery(
                        candidateQuery.intent(), candidateQuery.resource(), candidateQuery.operation(),
                        new GraduateKnowledgeFilters(comparisonScope, candidateQuery.degreeTypes(), candidateQuery.topicKeywords(), candidateQuery.filters().city()),
                        candidateQuery.aggregation(), candidateQuery.sort(), candidateQuery.limit(), context,
                        candidateQuery.detailLevel(), true, false);
                return GraduateFollowUpResolutionResult.resolved(compared, List.of("conversation memory"));
            }
            if (candidateQuery.filters().city() != null || !candidateQuery.resolvedUniversities().isEmpty()) {
                return GraduateFollowUpResolutionResult.unchanged(candidateQuery, List.of("current message"));
            }
            return GraduateFollowUpResolutionResult.clarificationRequired(
                    "LOCATION_SCOPE_REQUIRED",
                    candidateQuery,
                    List.of("current message")
            );
        }

        if (candidateQuery.intent() == GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP) {
            List<UniversityCatalog> catalogs = universityCatalogs == null ? List.of() : List.copyOf(universityCatalogs);
            List<ResolvedUniversity> explicitUniversities = GraduateKnowledgeResolutionSupport.resolveUniversities(
                    normalize(currentUserMessage), catalogs);
            if (!explicitUniversities.isEmpty()) {
                GraduateKnowledgeQuery replaced = new GraduateKnowledgeQuery(
                        candidateQuery.intent(), candidateQuery.resource(), candidateQuery.operation(),
                        new GraduateKnowledgeFilters(
                                explicitUniversities,
                                candidateQuery.degreeTypes(),
                                candidateQuery.topicKeywords(),
                                candidateQuery.filters().city(),
                                candidateQuery.filters().facultyName(),
                                candidateQuery.filters().departmentName()
                        ), candidateQuery.aggregation(), candidateQuery.sort(), candidateQuery.limit(),
                        candidateQuery.followUpContext(), candidateQuery.detailLevel(), true, false)
                        .withDecisionMetadata(GraduateKnowledgeInterpretationSource.FOLLOW_UP, GraduateKnowledgeAmbiguityReason.NONE, false);
                return GraduateFollowUpResolutionResult.resolved(replaced, List.of("current message"));
            }
            if (!candidateQuery.resolvedUniversities().isEmpty()) {
                return GraduateFollowUpResolutionResult.unchanged(candidateQuery, List.of("current message"));
            }
            return GraduateFollowUpResolutionResult.clarificationRequired(
                    "ACADEMIC_SCOPE_REQUIRED", candidateQuery, List.of("current message")
            );
        }

        String normalizedMessage = normalize(currentUserMessage);
        List<UniversityCatalog> catalogs = universityCatalogs == null ? List.of() : List.copyOf(universityCatalogs);
        GraduateKnowledgeResolutionSupport.HistorySignals historySignals =
                GraduateKnowledgeResolutionSupport.analyzeHistorySignals(recentConversationHistory, catalogs, conversationMemory);

        List<ResolvedUniversity> explicitUniversities = GraduateKnowledgeResolutionSupport.resolveUniversities(normalizedMessage, catalogs);
        boolean sameUniversityCue = GraduateKnowledgeResolutionSupport.containsStandaloneTokenOrPhrase(normalizedMessage, "same university", "the same university");
        boolean sameDegreeCue = GraduateKnowledgeResolutionSupport.containsStandaloneTokenOrPhrase(normalizedMessage, "same degree", "same program", "the same degree");
        DegreeReferenceSignal degreeReferenceSignal = detectDegreeReference(normalizedMessage);
        if (degreeReferenceSignal.unsupported()) {
            return GraduateFollowUpResolutionResult.unsupported("UNSUPPORTED_DEGREE_REFERENCE");
        }

        List<String> explicitDegreeTypes = degreeReferenceSignal.supportedDegreeTypes();
        boolean explicitUnresolvedDegreeReference = degreeReferenceSignal.explicitReference() && explicitDegreeTypes.isEmpty() && !sameDegreeCue;
        boolean currentTuitionIntent = GraduateKnowledgeResolutionSupport.detectTuitionAggregationIntent(normalizedMessage);
        boolean currentProgramIntent = GraduateKnowledgeResolutionSupport.detectProgramLookupIntent(normalizedMessage, explicitDegreeTypes, currentTuitionIntent);
        boolean followUpCue = GraduateKnowledgeResolutionSupport.isFollowUpMessage(normalizedMessage);
        boolean comparisonCue = GraduateKnowledgeResolutionSupport.containsAny(normalizedMessage, "compare", "comparison", "vs", "versus", "between")
                || (GraduateKnowledgeResolutionSupport.detectComparisonDimension(normalizedMessage) != null
                && GraduateKnowledgeResolutionSupport.containsAny(normalizedMessage, "which one", "which university", "more ", "cheaper", "second", "first"));
        boolean correctionCue = GraduateKnowledgeResolutionSupport.isCorrectionMessage(normalizedMessage);
        boolean explicitUniversityReference = hasExplicitUniversityReference(normalizedMessage) && !sameUniversityCue;

        List<ResolvedUniversity> memoryUniversities = memoryUniversities(conversationMemory);
        List<ResolvedUniversity> memoryComparisonUniversities = comparisonUniversities(conversationMemory);
        GraduateKnowledgeComparisonDimension requestedComparisonDimension =
                GraduateKnowledgeResolutionSupport.detectComparisonDimension(normalizedMessage);
        boolean comparisonStateChanged = conversationMemory != null
                && conversationMemory.comparisonActive()
                && !comparisonCue
                && !followUpCue
                && (currentProgramIntent || currentTuitionIntent || explicitUniversityReference
                || candidateQuery.operation() != GraduateKnowledgeOperation.COMPARE
                || requestedComparisonDimension != null
                && requestedComparisonDimension != conversationMemory.comparisonDimension());
        List<ResolvedUniversity> activeComparisonUniversities = !memoryComparisonUniversities.isEmpty()
                ? memoryComparisonUniversities
                : candidateQuery.resolvedUniversities();
        if (comparisonStateChanged) {
            activeComparisonUniversities = List.of();
        }
        boolean hasComparisonState = comparisonCue
                || (!comparisonStateChanged && conversationMemory != null && conversationMemory.comparisonActive() && activeComparisonUniversities.size() > 1)
                || candidateQuery.followUpResolved() && candidateQuery.resolvedUniversities().size() > 1;

        GraduateKnowledgeIntent resolvedIntent = resolveIntent(candidateQuery.intent(), currentTuitionIntent, currentProgramIntent, followUpCue, historySignals, conversationMemory);
        if (resolvedIntent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            return GraduateFollowUpResolutionResult.clarificationRequired("INTENT_REQUIRED", candidateQuery, List.of("current message", "recent history", "conversation memory"));
        }

        Integer ordinal = ordinalIndex(currentUserMessage);
        if (ordinal != null && candidateQuery.resource() != GraduateKnowledgeResource.UNIVERSITY
                && candidateQuery.resource() != GraduateKnowledgeResource.CAMPUS) {
            GraduateKnowledgeReferenceKind requiredKind = referenceKind(candidateQuery.resource());
            List<GraduateKnowledgeReference> allReferences = candidateQuery.followUpContext().references();
            List<GraduateKnowledgeReference> orderedReferences = allReferences.stream()
                    .filter(reference -> reference != null && reference.kind() == requiredKind)
                    .toList();
            if (allReferences.isEmpty()) {
                // Legacy university comparisons may have no typed rendered references yet;
                // retain the existing university antecedent resolution in that case.
                orderedReferences = null;
            }
            if (orderedReferences == null) {
                // fall through to the university-aware resolver below
            } else {
            if (orderedReferences.size() <= ordinal) {
                return GraduateFollowUpResolutionResult.clarificationRequired(
                        "RESOURCE_ORDER_REQUIRED", candidateQuery, List.of("rendered results", "conversation memory"));
            }
            GraduateKnowledgeReference selected = orderedReferences.get(ordinal);
            GraduateKnowledgeFollowUpContext selectedContext = new GraduateKnowledgeFollowUpContext(
                    candidateQuery.followUpContext().referencedUniversityId(), ordinal + 1,
                    candidateQuery.resource(), candidateQuery.operation(), List.of(selected),
                    candidateQuery.followUpContext().comparisonDimension());
            GraduateKnowledgeQuery selectedQuery = new GraduateKnowledgeQuery(
                    candidateQuery.intent(), candidateQuery.resource(), candidateQuery.operation(), candidateQuery.filters(),
                    candidateQuery.aggregation(), candidateQuery.sort(), candidateQuery.limit(), selectedContext,
                    candidateQuery.detailLevel(), true, false);
            return GraduateFollowUpResolutionResult.resolved(selectedQuery, List.of("rendered results", "conversation memory"));
            }
        }

        List<String> resolvedDegreeTypes = resolveDegreeTypes(
                candidateQuery.degreeTypes(),
                explicitDegreeTypes,
                sameDegreeCue,
                explicitUnresolvedDegreeReference,
                currentProgramIntent,
                currentTuitionIntent,
                historySignals,
                conversationMemory
        );
        if (resolvedDegreeTypes == null) {
            GraduateKnowledgeQuery clarificationQuery = new GraduateKnowledgeQuery(
                    candidateQuery.intent(),
                    candidateQuery.resolvedUniversities(),
                    List.of(),
                    candidateQuery.detailLevel(),
                    candidateQuery.followUpResolved() || followUpCue || correctionCue,
                    true
            );
            return GraduateFollowUpResolutionResult.clarificationRequired("DEGREE_REQUIRED", clarificationQuery, List.of("current message", "recent history", "conversation memory"));
        }

        UniversityResolution universityResolution = resolveUniversities(
                candidateQuery.resolvedUniversities(),
                explicitUniversities,
                followUpCue,
                comparisonCue,
                hasComparisonState,
                correctionCue,
                historySignals,
                memoryUniversities,
                activeComparisonUniversities,
                explicitUniversityReference,
                currentUserMessage
        );
        if (universityResolution.clarificationRequired) {
            GraduateKnowledgeQuery clarificationQuery = new GraduateKnowledgeQuery(
                    candidateQuery.intent(),
                    List.of(),
                    candidateQuery.degreeTypes(),
                    candidateQuery.detailLevel(),
                    candidateQuery.followUpResolved() || followUpCue || comparisonCue || correctionCue,
                    true
            );
            return GraduateFollowUpResolutionResult.clarificationRequired(universityResolution.reason, clarificationQuery, universityResolution.sources);
        }

        GraduateProgramDetailLevel detailLevel = resolveDetailLevel(candidateQuery.detailLevel(), normalizedMessage, resolvedIntent);
        boolean followUpResolved = candidateQuery.followUpResolved()
                || followUpCue
                || comparisonCue
                || correctionCue
                || !universityResolution.resolvedUniversities.equals(candidateQuery.resolvedUniversities())
                || !resolvedDegreeTypes.equals(candidateQuery.degreeTypes())
                || detailLevel != candidateQuery.detailLevel()
                || resolvedIntent != candidateQuery.intent();

        boolean ambiguous = shouldRemainAmbiguous(
                resolvedIntent,
                universityResolution.resolvedUniversities,
                resolvedDegreeTypes,
                followUpCue,
                comparisonCue,
                hasComparisonState,
                currentUserMessage
        );

        GraduateKnowledgeQuery resolvedQuery;
        if (hasProgramDetailFilters(candidateQuery.filters())
                || resolvedIntent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                || candidateQuery.operation() == GraduateKnowledgeOperation.COMPARE) {
            GraduateKnowledgeFilters filters = new GraduateKnowledgeFilters(
                    universityResolution.resolvedUniversities,
                    resolvedDegreeTypes,
                    candidateQuery.topicKeywords(),
                    candidateQuery.filters().city(),
                    candidateQuery.filters().facultyName(),
                    candidateQuery.filters().departmentName(),
                    candidateQuery.filters().languages(),
                    candidateQuery.filters().admissionRequirementTypes(),
                    candidateQuery.filters().programName(),
                    candidateQuery.filters().currency(),
                    candidateQuery.filters().billingBasis(),
                    candidateQuery.filters().academicYear(),
                    candidateQuery.filters().tuitionScopeLevel(),
                    candidateQuery.filters().thresholdOperator(),
                    candidateQuery.filters().thresholdValue()
            );
            GraduateKnowledgeResource effectiveResource = currentTuitionIntent
                    ? GraduateKnowledgeResource.PROGRAM : candidateQuery.resource();
            GraduateKnowledgeOperation effectiveOperation = currentTuitionIntent
                    ? GraduateKnowledgeOperation.AGGREGATE : candidateQuery.operation();
            GraduateKnowledgeIntent effectiveIntent = currentTuitionIntent
                    ? GraduateKnowledgeIntent.TUITION_AGGREGATION : candidateQuery.intent();
            resolvedQuery = new GraduateKnowledgeQuery(
                    effectiveIntent,
                    effectiveResource,
                    effectiveOperation,
                    filters,
                    currentTuitionIntent
                            ? GraduateKnowledgeQuery.aggregationFor(GraduateKnowledgeIntent.TUITION_AGGREGATION)
                            : candidateQuery.aggregation(),
                    candidateQuery.sort(),
                    candidateQuery.limit(),
                    universityResolution.resolvedUniversities.equals(candidateQuery.resolvedUniversities())
                            ? candidateQuery.followUpContext() : followUpContext(candidateQuery, universityResolution.resolvedUniversities),
                    detailLevel,
                    followUpResolved,
                    ambiguous
            ).withDecisionMetadata(
                    GraduateKnowledgeInterpretationSource.FOLLOW_UP,
                    ambiguous ? GraduateKnowledgeAmbiguityReason.UNRESOLVED_UNIVERSITY : GraduateKnowledgeAmbiguityReason.NONE,
                    ambiguous
            );
        } else {
            GraduateKnowledgeResource effectiveResource = candidateQuery.resource() == GraduateKnowledgeResource.NONE
                    ? GraduateKnowledgeQuery.resourceFor(resolvedIntent) : candidateQuery.resource();
            GraduateKnowledgeOperation effectiveOperation = candidateQuery.operation() == GraduateKnowledgeOperation.NONE
                    ? GraduateKnowledgeQuery.operationFor(resolvedIntent, detailLevel) : candidateQuery.operation();
            resolvedQuery = new GraduateKnowledgeQuery(
                    resolvedIntent, effectiveResource, effectiveOperation,
                    new GraduateKnowledgeFilters(universityResolution.resolvedUniversities, resolvedDegreeTypes, candidateQuery.topicKeywords()),
                    candidateQuery.aggregation(), candidateQuery.sort(), candidateQuery.limit(),
                    universityResolution.resolvedUniversities.equals(candidateQuery.resolvedUniversities())
                            ? candidateQuery.followUpContext() : followUpContext(candidateQuery, universityResolution.resolvedUniversities),
                    detailLevel, followUpResolved, ambiguous)
                    .withDecisionMetadata(GraduateKnowledgeInterpretationSource.FOLLOW_UP,
                            ambiguous ? GraduateKnowledgeAmbiguityReason.UNRESOLVED_UNIVERSITY : GraduateKnowledgeAmbiguityReason.NONE,
                            ambiguous);
        }

        if (ambiguous) {
            return GraduateFollowUpResolutionResult.clarificationRequired(
                    universityResolution.reason != null ? universityResolution.reason : "QUERY_REMAINS_AMBIGUOUS",
                    resolvedQuery,
                    universityResolution.sources
            );
        }

        if (sameQuery(candidateQuery, resolvedQuery)) {
            return GraduateFollowUpResolutionResult.unchanged(candidateQuery, universityResolution.sources);
        }
        return GraduateFollowUpResolutionResult.resolved(resolvedQuery, universityResolution.sources);
    }

    private GraduateKnowledgeIntent resolveIntent(
            GraduateKnowledgeIntent candidateIntent,
            boolean currentTuitionIntent,
            boolean currentProgramIntent,
            boolean followUpCue,
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            ConversationMemory conversationMemory
    ) {
        if (currentTuitionIntent) {
            return GraduateKnowledgeIntent.TUITION_AGGREGATION;
        }
        if (currentProgramIntent) {
            return GraduateKnowledgeIntent.PROGRAM_LOOKUP;
        }
        if (candidateIntent != null && candidateIntent != GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            return candidateIntent;
        }
        GraduateKnowledgeIntent historyIntent = historySignals.latestIntent();
        GraduateKnowledgeIntent memoryIntent = conversationMemory == null ? GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS : conversationMemory.lastIntentEnum();
        if (historyIntent != GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS
                && memoryIntent != GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS
                && historyIntent != memoryIntent) {
            return GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
        }
        if (followUpCue && historyIntent != GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            return historyIntent;
        }
        if (memoryIntent != GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            return memoryIntent;
        }
        return GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
    }

    private boolean hasProgramDetailFilters(GraduateKnowledgeFilters filters) {
        return filters != null
                && (filters.city() != null
                || filters.facultyName() != null
                || filters.departmentName() != null
                || !filters.topicKeywords().isEmpty()
                || !filters.languages().isEmpty()
                || !filters.admissionRequirementTypes().isEmpty()
                || filters.programName() != null);
    }

    private GraduateKnowledgeFollowUpContext followUpContext(
            GraduateKnowledgeQuery candidateQuery,
            List<ResolvedUniversity> universities
    ) {
        GraduateKnowledgeFollowUpContext current = candidateQuery.followUpContext();
        List<GraduateKnowledgeReference> references = current.references();
        if (references.isEmpty() && universities != null) {
            List<GraduateKnowledgeReference> ordered = new ArrayList<>();
            for (int index = 0; index < universities.size(); index++) {
                ResolvedUniversity university = universities.get(index);
                if (university != null) {
                    ordered.add(new GraduateKnowledgeReference(
                            GraduateKnowledgeReferenceKind.UNIVERSITY,
                            university.name(), university.acronym(), index + 1));
                }
            }
            references = ordered;
        }
        return new GraduateKnowledgeFollowUpContext(
                current.referencedUniversityId(),
                current.referencedResultOrdinal(),
                candidateQuery.resource(),
                candidateQuery.operation(),
                references,
                current.comparisonDimension()
        );
    }

    private GraduateKnowledgeAggregation safeTuitionAggregation(GraduateKnowledgeQuery query) {
        if (query.aggregation() == null || query.aggregation().function() == GraduateKnowledgeAggregationFunction.NONE
                || query.aggregation().function() == GraduateKnowledgeAggregationFunction.COUNT) {
            return GraduateKnowledgeQuery.aggregationFor(GraduateKnowledgeIntent.TUITION_AGGREGATION);
        }
        return query.aggregation();
    }

    private List<String> resolveDegreeTypes(
            List<String> candidateDegreeTypes,
            List<String> explicitDegreeTypes,
            boolean sameDegreeCue,
            boolean explicitUnresolvedDegreeReference,
            boolean currentProgramIntent,
            boolean currentTuitionIntent,
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            ConversationMemory conversationMemory
    ) {
        if (!explicitDegreeTypes.isEmpty()) {
            return explicitDegreeTypes;
        }
        if (sameDegreeCue) {
            String uniqueDegree = uniqueDegree(historySignals, conversationMemory);
            return uniqueDegree == null ? null : List.of(uniqueDegree);
        }
        if (explicitUnresolvedDegreeReference) {
            return null;
        }
        if (candidateDegreeTypes != null && !candidateDegreeTypes.isEmpty()) {
            return candidateDegreeTypes;
        }

        String historyDegree = historySignals.latestDegreeType();
        String memoryDegree = uniqueDegreeFromMemory(conversationMemory);
        if (historyDegree != null && memoryDegree != null && !historyDegree.equalsIgnoreCase(memoryDegree)) {
            return null;
        }

        String inherited = historyDegree != null ? historyDegree : memoryDegree;
        if (inherited != null) {
            return List.of(inherited);
        }

        if (currentProgramIntent || currentTuitionIntent) {
            return List.of();
        }
        return candidateDegreeTypes == null ? List.of() : candidateDegreeTypes;
    }

    private UniversityResolution resolveUniversities(
            List<ResolvedUniversity> candidateUniversities,
            List<ResolvedUniversity> explicitUniversities,
            boolean followUpCue,
            boolean comparisonCue,
            boolean hasComparisonState,
            boolean correctionCue,
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            List<ResolvedUniversity> memoryUniversities,
            List<ResolvedUniversity> activeComparisonUniversities,
            boolean explicitUniversityReference,
            String currentUserMessage
    ) {
        if (!explicitUniversities.isEmpty()) {
            List<ResolvedUniversity> resolved = GraduateKnowledgeResolutionSupport.distinctUniversities(explicitUniversities);
            if (comparisonCue && resolved.size() == 1) {
                ResolvedUniversity antecedent = latestComparableAntecedent(historySignals, memoryUniversities, resolved.get(0));
                if (antecedent != null) {
                    return UniversityResolution.resolved(GraduateKnowledgeResolutionSupport.distinctUniversities(List.of(antecedent, resolved.get(0))), List.of("current message", "recent history"));
                }
                if (hasComparisonState && activeComparisonUniversities.size() > 1) {
                    ResolvedUniversity other = firstDifferent(activeComparisonUniversities, resolved.get(0));
                    if (other != null) {
                        return UniversityResolution.resolved(GraduateKnowledgeResolutionSupport.distinctUniversities(List.of(other, resolved.get(0))), List.of("current message", "conversation memory"));
                    }
                }
                return UniversityResolution.clarification("COMPARISON_TARGET_REQUIRED", GraduateKnowledgeResolutionSupport.distinctUniversities(resolved), List.of("current message"));
            }
            return UniversityResolution.resolved(resolved, List.of("current message"));
        }

        Integer ordinalIndex = ordinalIndex(currentUserMessage);
        if (ordinalIndex != null) {
            if (activeComparisonUniversities.size() > ordinalIndex) {
                return UniversityResolution.resolved(List.of(activeComparisonUniversities.get(ordinalIndex)), List.of("conversation memory"));
            }
            List<ResolvedUniversity> antecedentPair = pairFromHistoryOrMemory(historySignals, memoryUniversities);
            if (antecedentPair.size() > ordinalIndex) {
                return UniversityResolution.resolved(List.of(antecedentPair.get(ordinalIndex)), List.of("recent history", "conversation memory"));
            }
            return UniversityResolution.clarification("COMPARISON_ORDER_REQUIRED", candidateUniversities, List.of("conversation memory", "recent history"));
        }

        if (explicitUniversityReference) {
            return UniversityResolution.clarification("UNIVERSITY_REQUIRED", candidateUniversities, List.of("current message", "recent history", "conversation memory"));
        }

        boolean singularReferenceCue = followUpCue && GraduateKnowledgeResolutionSupport.containsStandaloneTokenOrPhrase(normalize(currentUserMessage), "it", "there", "that", "that one", "those", "them", "same");
        if (singularReferenceCue && hasComparisonState && activeComparisonUniversities.size() > 1) {
            return UniversityResolution.clarification("COMPARISON_TARGET_REQUIRED", candidateUniversities, List.of("conversation memory", "recent history"));
        }

        if (comparisonCue) {
            if (activeComparisonUniversities.size() >= 2) {
                return UniversityResolution.resolved(GraduateKnowledgeResolutionSupport.distinctUniversities(activeComparisonUniversities), List.of("conversation memory"));
            }

            List<ResolvedUniversity> antecedentPair = pairFromHistoryOrMemory(historySignals, memoryUniversities);
            if (antecedentPair.size() == 2) {
                return UniversityResolution.resolved(GraduateKnowledgeResolutionSupport.distinctUniversities(antecedentPair), List.of("recent history", "conversation memory"));
            }
            return UniversityResolution.clarification("COMPARISON_TARGET_REQUIRED", candidateUniversities, List.of("current message", "recent history", "conversation memory"));
        }

        if (hasComparisonState && !candidateUniversities.isEmpty()) {
            return UniversityResolution.resolved(GraduateKnowledgeResolutionSupport.distinctUniversities(candidateUniversities), List.of("current message"));
        }

        if (followUpCue) {
            ResolvedUniversity uniqueHistoryUniversity = uniqueUniversity(historySignals, memoryUniversities);
            if (uniqueHistoryUniversity != null) {
                return UniversityResolution.resolved(List.of(uniqueHistoryUniversity), List.of(historySignals.latestUniversity() != null ? "recent history" : "conversation memory"));
            }
            if (historySignals.distinctUniversityCount() > 1) {
                return UniversityResolution.clarification("UNIVERSITY_AMBIGUOUS", candidateUniversities, List.of("recent history", "conversation memory"));
            }
        }

        if (!candidateUniversities.isEmpty()) {
            return UniversityResolution.resolved(GraduateKnowledgeResolutionSupport.distinctUniversities(candidateUniversities), List.of("current message"));
        }

        ResolvedUniversity uniqueHistoryUniversity = uniqueUniversity(historySignals, memoryUniversities);
        if (uniqueHistoryUniversity != null) {
            return UniversityResolution.resolved(List.of(uniqueHistoryUniversity), List.of(historySignals.latestUniversity() != null ? "recent history" : "conversation memory"));
        }

        return UniversityResolution.clarification("UNIVERSITY_REQUIRED", candidateUniversities, List.of("current message", "recent history", "conversation memory"));
    }

    private GraduateProgramDetailLevel resolveDetailLevel(GraduateProgramDetailLevel candidateDetailLevel, String normalizedMessage, GraduateKnowledgeIntent intent) {
        if (intent != GraduateKnowledgeIntent.PROGRAM_LOOKUP) {
            return null;
        }
        if (GraduateKnowledgeResolutionSupport.containsAny(normalizedMessage,
                "detail",
                "details",
                "admission",
                "admissions",
                "tuition",
                "fee",
                "fees",
                "requirement",
                "requirements",
                "language",
                "credits",
                "credit",
                "duration",
                "thesis",
                "track",
                "curriculum",
                "deadline",
                "application")) {
            return GraduateProgramDetailLevel.DETAILS;
        }
        return candidateDetailLevel == null ? GraduateProgramDetailLevel.LIST : candidateDetailLevel;
    }

    private boolean shouldRemainAmbiguous(
            GraduateKnowledgeIntent intent,
            List<ResolvedUniversity> universities,
            List<String> degreeTypes,
            boolean followUpCue,
            boolean comparisonCue,
            boolean hasComparisonState,
            String currentUserMessage
    ) {
        if (intent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            return true;
        }
        if ((intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP || intent == GraduateKnowledgeIntent.TUITION_AGGREGATION) && universities.isEmpty()) {
            return true;
        }
        if (GraduateKnowledgeResolutionSupport.containsAny(normalize(currentUserMessage), "same degree") && degreeTypes.size() != 1) {
            return true;
        }
        if (comparisonCue && universities.size() < 2 && !hasComparisonState) {
            return true;
        }
        if (followUpCue && universities.size() > 1 && !comparisonCue) {
            return true;
        }
        return false;
    }

    private ResolvedUniversity uniqueUniversity(GraduateKnowledgeResolutionSupport.HistorySignals historySignals, List<ResolvedUniversity> memoryUniversities) {
        ResolvedUniversity history = historySignals.latestUniversity();
        ResolvedUniversity memory = memoryUniversities.size() == 1 ? memoryUniversities.get(0) : null;
        if (history != null && memory != null && !GraduateKnowledgeResolutionSupport.sameUniversity(history, memory)) {
            return null;
        }
        if (history != null) {
            return history;
        }
        if (memoryUniversities.size() == 1) {
            return memoryUniversities.get(0);
        }
        return null;
    }

    private String uniqueDegree(GraduateKnowledgeResolutionSupport.HistorySignals historySignals, ConversationMemory conversationMemory) {
        String history = historySignals.latestDegreeType();
        String memory = uniqueDegreeFromMemory(conversationMemory);
        if (history != null && memory != null && !history.equalsIgnoreCase(memory)) {
            return null;
        }
        return history != null ? history : memory;
    }

    private String uniqueDegreeFromMemory(ConversationMemory conversationMemory) {
        if (conversationMemory == null || conversationMemory.activeDegreeTypes() == null || conversationMemory.activeDegreeTypes().isEmpty()) {
            return null;
        }
        LinkedHashSet<String> degrees = new LinkedHashSet<>();
        for (String degree : conversationMemory.activeDegreeTypes()) {
            if (degree != null && !degree.isBlank()) {
                degrees.add(degree.trim().toUpperCase(Locale.ROOT));
            }
        }
        if (degrees.size() != 1) {
            return null;
        }
        return degrees.iterator().next();
    }

    private List<ResolvedUniversity> memoryUniversities(ConversationMemory conversationMemory) {
        if (conversationMemory == null || conversationMemory.activeUniversities() == null || conversationMemory.activeUniversities().isEmpty()) {
            return List.of();
        }
        List<ResolvedUniversity> universities = new ArrayList<>();
        for (MemoryUniversityRef ref : conversationMemory.activeUniversities()) {
            if (ref != null && ref.id() != null) {
                universities.add(new ResolvedUniversity(ref.id(), ref.name(), ref.acronym()));
            }
        }
        return GraduateKnowledgeResolutionSupport.distinctUniversities(universities);
    }

    private List<ResolvedUniversity> comparisonUniversities(ConversationMemory conversationMemory) {
        if (conversationMemory == null || conversationMemory.comparisonUniversities() == null || conversationMemory.comparisonUniversities().isEmpty()) {
            return List.of();
        }
        List<ResolvedUniversity> universities = new ArrayList<>();
        for (MemoryUniversityRef ref : conversationMemory.comparisonUniversities()) {
            if (ref != null && ref.id() != null) {
                universities.add(new ResolvedUniversity(ref.id(), ref.name(), ref.acronym()));
            }
        }
        return GraduateKnowledgeResolutionSupport.distinctUniversities(universities);
    }

    private ResolvedUniversity latestComparableAntecedent(
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            List<ResolvedUniversity> memoryUniversities,
            ResolvedUniversity currentExplicitUniversity
    ) {
        ResolvedUniversity history = historySignals.latestUniversity();
        if (history != null && !GraduateKnowledgeResolutionSupport.sameUniversity(history, currentExplicitUniversity)) {
            return history;
        }
        for (ResolvedUniversity university : memoryUniversities) {
            if (university != null && !GraduateKnowledgeResolutionSupport.sameUniversity(university, currentExplicitUniversity)) {
                return university;
            }
        }
        return null;
    }

    private List<ResolvedUniversity> pairFromHistoryOrMemory(
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            List<ResolvedUniversity> memoryUniversities
    ) {
        if (historySignals.distinctUniversityCount() == 2) {
            return historySignals.distinctUniversities();
        }
        if (memoryUniversities.size() == 2) {
            return memoryUniversities;
        }
        return List.of();
    }

    private ResolvedUniversity firstDifferent(List<ResolvedUniversity> universities, ResolvedUniversity current) {
        for (ResolvedUniversity university : universities) {
            if (university != null && !GraduateKnowledgeResolutionSupport.sameUniversity(university, current)) {
                return university;
            }
        }
        return null;
    }

    private Integer ordinalIndex(String message) {
        String normalized = normalize(message);
        if (GraduateKnowledgeResolutionSupport.containsAny(normalized, "first", "first one", "first university")) {
            return 0;
        }
        if (GraduateKnowledgeResolutionSupport.containsAny(normalized, "second", "second one", "second university")) {
            return 1;
        }
        return null;
    }

    private GraduateKnowledgeReferenceKind referenceKind(GraduateKnowledgeResource resource) {
        return switch (resource) {
            case CAMPUS -> GraduateKnowledgeReferenceKind.CAMPUS;
            case PROGRAM -> GraduateKnowledgeReferenceKind.PROGRAM;
            case FACULTY -> GraduateKnowledgeReferenceKind.FACULTY;
            case DEPARTMENT -> GraduateKnowledgeReferenceKind.DEPARTMENT;
            default -> GraduateKnowledgeReferenceKind.UNIVERSITY;
        };
    }

    private boolean sameQuery(GraduateKnowledgeQuery left, GraduateKnowledgeQuery right) {
        return left.resource() == right.resource()
                && left.operation() == right.operation()
                && left.scope() == right.scope()
                && Objects.equals(left.filters(), right.filters())
                && Objects.equals(left.aggregation(), right.aggregation())
                && Objects.equals(left.sort(), right.sort())
                && Objects.equals(left.limit(), right.limit())
                && Objects.equals(left.followUpContext(), right.followUpContext())
                && left.detailLevel() == right.detailLevel()
                && left.ambiguous() == right.ambiguous();
    }

    private boolean hasExplicitUniversityReference(String normalizedMessage) {
        return GraduateKnowledgeResolutionSupport.containsStandaloneTokenOrPhrase(
                normalizedMessage,
                "university",
                "college",
                "institute",
                "school",
                "faculty",
                "department"
        );
    }

    private DegreeReferenceSignal detectDegreeReference(String normalizedMessage) {
        if (normalizedMessage == null || normalizedMessage.isBlank()) {
            return new DegreeReferenceSignal(List.of(), false, false);
        }

        List<String> supportedDegrees = GraduateKnowledgeResolutionSupport.detectDegreeTypes(normalizedMessage);
        boolean unsupportedDegree = GraduateKnowledgeResolutionSupport.containsStandaloneTokenOrPhrase(
                normalizedMessage,
                "bachelor",
                "bachelors",
                "bachelor's",
                "undergraduate",
                "undergrad"
        );
        boolean degreeCue = GraduateKnowledgeResolutionSupport.containsStandaloneTokenOrPhrase(
                normalizedMessage,
                "degree",
                "diploma",
                "certificate",
                "master",
                "masters",
                "master's",
                "mba",
                "phd",
                "doctorate",
                "doctoral",
                "bachelor",
                "bachelors",
                "bachelor's",
                "undergraduate",
                "undergrad"
        );
        return new DegreeReferenceSignal(supportedDegrees, unsupportedDegree, degreeCue || !supportedDegrees.isEmpty());
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private record UniversityResolution(
            List<ResolvedUniversity> resolvedUniversities,
            boolean clarificationRequired,
            String reason,
            List<String> sources
    ) {
        private static UniversityResolution resolved(List<ResolvedUniversity> resolvedUniversities, List<String> sources) {
            return new UniversityResolution(GraduateKnowledgeResolutionSupport.distinctUniversities(resolvedUniversities), false, "", sources);
        }

        private static UniversityResolution clarification(String reason, List<ResolvedUniversity> resolvedUniversities, List<String> sources) {
            return new UniversityResolution(GraduateKnowledgeResolutionSupport.distinctUniversities(resolvedUniversities), true, reason, sources);
        }
    }

    private record DegreeReferenceSignal(
            List<String> supportedDegreeTypes,
            boolean unsupported,
            boolean explicitReference
    ) {
        private DegreeReferenceSignal {
            supportedDegreeTypes = supportedDegreeTypes == null ? List.of() : List.copyOf(supportedDegreeTypes);
        }
    }
}
