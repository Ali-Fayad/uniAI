package com.uniai.chat.application.retrieval;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.catalog.domain.model.UniversityCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GraduateKnowledgeQueryInterpreter {

    public GraduateKnowledgeQuery interpret(
            String userMessage,
            List<AiConversationMessage> recentConversationHistory,
            List<UniversityCatalog> universityCatalogs
    ) {
        return interpret(userMessage, recentConversationHistory, universityCatalogs, ConversationMemory.empty());
    }

    public GraduateKnowledgeQuery interpret(
            String userMessage,
            List<AiConversationMessage> recentConversationHistory,
            List<UniversityCatalog> universityCatalogs,
            ConversationMemory conversationMemory
    ) {
        String normalizedMessage = normalize(userMessage);
        List<UniversityCatalog> catalog = universityCatalogs == null ? List.of() : List.copyOf(universityCatalogs);
        List<ResolvedUniversity> currentUniversities = resolveUniversities(normalizedMessage, catalog);
        GraduateKnowledgeContextPolicy contextPolicy = GraduateKnowledgeContextPolicyClassifier.classify(normalizedMessage, currentUniversities);
        GraduateKnowledgeResolutionSupport.HistorySignals historySignals = GraduateKnowledgeResolutionSupport.analyzeHistorySignals(
                recentConversationHistory, catalog, conversationMemory, contextPolicy);
        List<String> currentDegreeTypes = detectDegreeTypes(normalizedMessage);
        boolean currentTuitionIntent = detectTuitionAggregationIntent(normalizedMessage);
        boolean currentProgramIntent = detectProgramLookupIntent(normalizedMessage, currentDegreeTypes, currentTuitionIntent);
        boolean currentLocationIntent = GraduateKnowledgeResolutionSupport.detectLocationLookupIntent(normalizedMessage);
        GraduateKnowledgeResource currentLocationResource = GraduateKnowledgeResolutionSupport.detectLocationResource(normalizedMessage);
        GraduateKnowledgeOperation currentLocationOperation = GraduateKnowledgeResolutionSupport.detectLocationOperation(normalizedMessage);
        String currentCity = GraduateKnowledgeResolutionSupport.detectRequestedCity(normalizedMessage, catalog);
        boolean currentAcademicIntent = GraduateKnowledgeResolutionSupport.detectAcademicStructureLookupIntent(normalizedMessage);
        GraduateKnowledgeResource currentAcademicResource = GraduateKnowledgeResolutionSupport.detectAcademicResource(normalizedMessage);
        GraduateKnowledgeOperation currentAcademicOperation = GraduateKnowledgeResolutionSupport.detectAcademicOperation(normalizedMessage);
        String currentFacultyName = GraduateKnowledgeResolutionSupport.detectFacultyName(normalizedMessage);
        String currentDepartmentName = GraduateKnowledgeResolutionSupport.detectDepartmentName(normalizedMessage);
        List<String> currentTopicKeywords = GraduateKnowledgeResolutionSupport.detectTopicKeywords(normalizedMessage);
        List<String> currentLanguages = GraduateKnowledgeResolutionSupport.detectLanguages(normalizedMessage);
        List<String> currentAdmissionTypes = GraduateKnowledgeResolutionSupport.detectAdmissionRequirementTypes(normalizedMessage);
        String currentProgramName = GraduateKnowledgeResolutionSupport.detectProgramName(normalizedMessage);
        GraduateKnowledgeComparisonDimension currentComparisonDimension = GraduateKnowledgeResolutionSupport.detectComparisonDimension(normalizedMessage);
        boolean currentOverviewIntent = GraduateKnowledgeResolutionSupport.detectGraduateOverviewIntent(normalizedMessage, currentProgramIntent, currentTuitionIntent);
        boolean currentFollowUp = isFollowUpMessage(normalizedMessage);
        boolean explicitComparison = containsAny(normalizedMessage, "compare", "comparison", "vs", "versus", "between")
                || (currentComparisonDimension != null
                && containsAny(normalizedMessage, "which one", "which university", "more ", "cheaper", "second", "first"));

        if (GraduateKnowledgeResolutionSupport.isSubjectiveComparison(normalizedMessage)) {
            return new GraduateKnowledgeQuery(
                    GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS,
                    List.of(), List.of(), null, false, true
            );
        }

        if (isDeterministicGeneralChatMessage(normalizedMessage)
                && currentUniversities.isEmpty()
                && currentDegreeTypes.isEmpty()
                && !currentTuitionIntent
                && !currentProgramIntent
                && !currentOverviewIntent
                && !currentFollowUp
                && !explicitComparison) {
            return new GraduateKnowledgeQuery(
                    GraduateKnowledgeIntent.GENERAL_CHAT,
                    List.of(),
                    List.of(),
                    null,
                    false,
                    false
            );
        }

        GraduateKnowledgeIntent intent = GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
        GraduateProgramDetailLevel detailLevel = GraduateProgramDetailLevel.LIST;

        boolean followUpResolved = false;
        boolean ambiguous = false;
        List<ResolvedUniversity> resolvedUniversities = new ArrayList<>();
        List<String> resolvedDegreeTypes = new ArrayList<>();

        GraduateKnowledgeIntent inheritedHistoryIntent = historySignals.latestIntent();

        if (currentAcademicIntent) {
            intent = GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP;
            resolvedUniversities = GraduateKnowledgeResolutionSupport.distinctUniversities(currentUniversities);
            resolvedDegreeTypes = new ArrayList<>(currentDegreeTypes);
            ambiguous = resolvedUniversities.isEmpty();
        } else if (currentLocationIntent && !currentProgramIntent && !currentTuitionIntent) {
            intent = GraduateKnowledgeIntent.LOCATION_LOOKUP;
            resolvedUniversities = GraduateKnowledgeResolutionSupport.distinctUniversities(currentUniversities);
            resolvedDegreeTypes = List.of();
            followUpResolved = currentFollowUp;
            ambiguous = resolvedUniversities.isEmpty()
                    && currentCity == null
                    && currentLocationOperation != GraduateKnowledgeOperation.COUNT;
        } else if (currentOverviewIntent
                || (currentFollowUp && inheritedHistoryIntent == GraduateKnowledgeIntent.GRADUATE_OVERVIEW)) {
            intent = GraduateKnowledgeIntent.GRADUATE_OVERVIEW;
            followUpResolved = currentFollowUp || (currentOverviewIntent && currentUniversities.isEmpty() && historySignals.latestUniversity() != null);
            resolvedUniversities = resolveOverviewUniversities(currentUniversities, historySignals);
            resolvedDegreeTypes = currentFollowUp
                    ? resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals)
                    : new ArrayList<>(currentDegreeTypes);
            ambiguous = resolvedUniversities.isEmpty();
        } else if (currentTuitionIntent
                || (currentFollowUp && inheritedHistoryIntent == GraduateKnowledgeIntent.TUITION_AGGREGATION)
                || (explicitComparison && inheritedHistoryIntent == GraduateKnowledgeIntent.TUITION_AGGREGATION && currentUniversities.isEmpty())) {
            intent = GraduateKnowledgeIntent.TUITION_AGGREGATION;
            followUpResolved = currentFollowUp;
            resolvedUniversities = resolveTuitionUniversities(normalizedMessage, currentUniversities, historySignals, explicitComparison);
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals);
            ambiguous = shouldFlagAmbiguousTuition(normalizedMessage, currentUniversities, historySignals, explicitComparison, resolvedUniversities);
            if (currentCity != null) ambiguous = false;
        } else if (currentProgramIntent
                || (currentFollowUp && inheritedHistoryIntent == GraduateKnowledgeIntent.PROGRAM_LOOKUP)
                || (!currentUniversities.isEmpty() && currentFollowUp)) {
            intent = GraduateKnowledgeIntent.PROGRAM_LOOKUP;
            followUpResolved = currentFollowUp;
            resolvedUniversities = resolveProgramUniversities(normalizedMessage, currentUniversities, historySignals, explicitComparison);
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals);
            detailLevel = resolveDetailLevel(normalizedMessage);
            ambiguous = shouldFlagAmbiguousProgram(normalizedMessage, currentUniversities, historySignals, explicitComparison, resolvedUniversities);
            if (currentCity != null) ambiguous = false;
        } else if (explicitComparison && !currentUniversities.isEmpty()) {
            intent = inheritedHistoryIntent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                    ? GraduateKnowledgeIntent.TUITION_AGGREGATION
                    : GraduateKnowledgeIntent.PROGRAM_LOOKUP;
            followUpResolved = currentFollowUp || explicitComparison;
            resolvedUniversities = intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                    ? resolveTuitionUniversities(normalizedMessage, currentUniversities, historySignals, true)
                    : resolveProgramUniversities(normalizedMessage, currentUniversities, historySignals, true);
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals);
            if (intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP) {
                detailLevel = resolveDetailLevel(normalizedMessage);
            }
            ambiguous = shouldFlagAmbiguousBranch(currentUniversities, historySignals, explicitComparison, resolvedUniversities);
        } else if (!currentUniversities.isEmpty() && !currentDegreeTypes.isEmpty()) {
            intent = GraduateKnowledgeIntent.PROGRAM_LOOKUP;
            resolvedUniversities = GraduateKnowledgeResolutionSupport.distinctUniversities(currentUniversities);
            resolvedDegreeTypes = new ArrayList<>(currentDegreeTypes);
            detailLevel = resolveDetailLevel(normalizedMessage);
        } else if (historySignals.canInheritProgramIntent(currentFollowUp)) {
            intent = GraduateKnowledgeIntent.PROGRAM_LOOKUP;
            followUpResolved = true;
            resolvedUniversities = !currentUniversities.isEmpty()
                    ? GraduateKnowledgeResolutionSupport.distinctUniversities(currentUniversities)
                    : historySignals.latestUniversity() == null ? List.of() : List.of(historySignals.latestUniversity());
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals);
            detailLevel = resolveDetailLevel(normalizedMessage);
            ambiguous = resolvedUniversities.isEmpty();
        } else if (historySignals.canInheritTuitionIntent(currentFollowUp)) {
            intent = GraduateKnowledgeIntent.TUITION_AGGREGATION;
            followUpResolved = true;
            resolvedUniversities = historySignals.latestUniversity() == null ? List.of() : List.of(historySignals.latestUniversity());
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals);
            ambiguous = resolvedUniversities.isEmpty();
        } else if (!currentUniversities.isEmpty()) {
            ambiguous = true;
        }

        if (intent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            return new GraduateKnowledgeQuery(
                    intent,
                    List.of(),
                    List.of(),
                    null,
                    followUpResolved,
                    true
            );
        }

        if (!explicitComparison && currentUniversities.size() > 1) {
            ambiguous = true;
        }

        if (resolvedUniversities.isEmpty()
                && currentCity == null
                && !(intent == GraduateKnowledgeIntent.LOCATION_LOOKUP
                && currentLocationOperation == GraduateKnowledgeOperation.COUNT)) {
            ambiguous = true;
        }

        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                && historySignals.hasMultipleDistinctUniversities()
                && currentUniversities.isEmpty()
                && !explicitComparison) {
            ambiguous = true;
        }

        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                && containsAny(normalizedMessage, "cheaper", "cheapest", "cheaper than", "less expensive")
                && historySignals.distinctUniversityCount() == 2
                && currentUniversities.isEmpty()) {
            resolvedUniversities = historySignals.distinctUniversities();
            ambiguous = false;
        }

        if (intent == GraduateKnowledgeIntent.LOCATION_LOOKUP) {
            return new GraduateKnowledgeQuery(
                    intent,
                    currentLocationResource,
                    explicitComparison ? GraduateKnowledgeOperation.COMPARE : currentLocationOperation,
                    new GraduateKnowledgeFilters(resolvedUniversities, List.of(), List.of(), currentCity),
                    GraduateKnowledgeAggregation.empty(),
                    GraduateKnowledgeSort.empty(),
                    null,
                    explicitComparison ? new GraduateKnowledgeFollowUpContext(null, null, currentLocationResource, GraduateKnowledgeOperation.COMPARE, List.of(), currentComparisonDimension) : null,
                    null,
                    followUpResolved,
                    ambiguous
            );
        }

        if (intent == GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP) {
            return new GraduateKnowledgeQuery(
                    intent,
                    currentAcademicResource,
                    explicitComparison ? GraduateKnowledgeOperation.COMPARE : currentAcademicOperation,
                    new GraduateKnowledgeFilters(
                            resolvedUniversities,
                            resolvedDegreeTypes,
                            currentTopicKeywords,
                            null,
                            currentFacultyName,
                            currentDepartmentName
                    ),
                    GraduateKnowledgeAggregation.empty(),
                    GraduateKnowledgeSort.empty(),
                    null,
                    explicitComparison ? new GraduateKnowledgeFollowUpContext(null, null, currentAcademicResource, GraduateKnowledgeOperation.COMPARE, List.of(), currentComparisonDimension) : null,
                    null,
                    followUpResolved,
                    ambiguous
            );
        }

        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION) {
            GraduateKnowledgeFilters tuitionFilters = new GraduateKnowledgeFilters(
                    resolvedUniversities,
                    resolvedDegreeTypes,
                    currentTopicKeywords,
                    currentCity,
                    currentFacultyName,
                    currentDepartmentName,
                    currentLanguages,
                    currentAdmissionTypes,
                    currentProgramName,
                    GraduateKnowledgeResolutionSupport.detectTuitionCurrency(normalizedMessage),
                    GraduateKnowledgeResolutionSupport.detectTuitionBillingBasis(normalizedMessage),
                    GraduateKnowledgeResolutionSupport.detectTuitionAcademicYear(normalizedMessage),
                    GraduateKnowledgeResolutionSupport.detectTuitionScope(normalizedMessage),
                    GraduateKnowledgeResolutionSupport.detectTuitionThresholdOperator(normalizedMessage),
                    GraduateKnowledgeResolutionSupport.detectTuitionThresholdValue(normalizedMessage)
            );
            GraduateKnowledgeAggregationFunction function = GraduateKnowledgeResolutionSupport.detectTuitionAggregation(normalizedMessage);
            GraduateKnowledgeSort sort = GraduateKnowledgeResolutionSupport.detectTuitionSort(normalizedMessage);
            Integer limit = GraduateKnowledgeResolutionSupport.detectTuitionLimit(normalizedMessage);
            return new GraduateKnowledgeQuery(
                    intent,
                    GraduateKnowledgeResource.PROGRAM,
                    explicitComparison ? GraduateKnowledgeOperation.COMPARE : GraduateKnowledgeOperation.AGGREGATE,
                    tuitionFilters,
                    new GraduateKnowledgeAggregation(function, "tuition"),
                    sort,
                    limit,
                    explicitComparison
                            ? new GraduateKnowledgeFollowUpContext(null, null, GraduateKnowledgeResource.PROGRAM, GraduateKnowledgeOperation.COMPARE, List.of(), currentComparisonDimension)
                            : GraduateKnowledgeFollowUpContext.empty(),
                    null,
                    followUpResolved,
                    ambiguous
            );
        }
        if (intent != GraduateKnowledgeIntent.PROGRAM_LOOKUP) {
            return new GraduateKnowledgeQuery(
                    intent,
                    resolvedUniversities,
                    resolvedDegreeTypes,
                    null,
                    followUpResolved,
                    ambiguous
            );
        }
        GraduateKnowledgeFilters filters = new GraduateKnowledgeFilters(
                resolvedUniversities,
                resolvedDegreeTypes,
                currentTopicKeywords,
                currentCity,
                currentFacultyName,
                currentDepartmentName,
                currentLanguages,
                currentAdmissionTypes,
                currentProgramName
        );
        return new GraduateKnowledgeQuery(
                intent,
                GraduateKnowledgeResource.PROGRAM,
                intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP
                        ? (explicitComparison ? GraduateKnowledgeOperation.COMPARE : GraduateKnowledgeOperation.LIST) : GraduateKnowledgeOperation.NONE,
                filters,
                GraduateKnowledgeAggregation.empty(),
                GraduateKnowledgeSort.empty(),
                intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP ? GraduateKnowledgeQuery.MAX_LIMIT : null,
                explicitComparison
                        ? new GraduateKnowledgeFollowUpContext(null, null, GraduateKnowledgeResource.PROGRAM, GraduateKnowledgeOperation.COMPARE, List.of(), currentComparisonDimension)
                        : GraduateKnowledgeFollowUpContext.empty(),
                intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP ? detailLevel : null,
                followUpResolved,
                ambiguous
        );
    }

    private List<ResolvedUniversity> resolveOverviewUniversities(
            List<ResolvedUniversity> currentUniversities,
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals
    ) {
        if (!currentUniversities.isEmpty()) {
            return GraduateKnowledgeResolutionSupport.distinctUniversities(currentUniversities);
        }
        if (historySignals.latestUniversity() != null) {
            return List.of(historySignals.latestUniversity());
        }
        return List.of();
    }

    private GraduateProgramDetailLevel resolveDetailLevel(String normalizedMessage) {
        if (containsAny(normalizedMessage,
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
        return GraduateProgramDetailLevel.LIST;
    }

    private List<ResolvedUniversity> resolveProgramUniversities(
            String normalizedMessage,
            List<ResolvedUniversity> currentUniversities,
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            boolean explicitComparison
    ) {
        if (!currentUniversities.isEmpty()) {
            if (explicitComparison
                    && currentUniversities.size() == 1
                    && historySignals.latestUniversity() != null
                    && !GraduateKnowledgeResolutionSupport.sameUniversity(historySignals.latestUniversity(), currentUniversities.get(0))) {
                return GraduateKnowledgeResolutionSupport.distinctUniversities(List.of(historySignals.latestUniversity(), currentUniversities.get(0)));
            }
            return GraduateKnowledgeResolutionSupport.distinctUniversities(currentUniversities);
        }

        if (historySignals.latestUniversity() != null && isFollowUpMessage(normalizedMessage)) {
            return List.of(historySignals.latestUniversity());
        }

        return List.of();
    }

    private List<ResolvedUniversity> resolveTuitionUniversities(
            String normalizedMessage,
            List<ResolvedUniversity> currentUniversities,
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            boolean explicitComparison
    ) {
        if (!currentUniversities.isEmpty()) {
            if (explicitComparison
                    && currentUniversities.size() == 1
                    && historySignals.latestUniversity() != null
                    && !GraduateKnowledgeResolutionSupport.sameUniversity(historySignals.latestUniversity(), currentUniversities.get(0))) {
                return GraduateKnowledgeResolutionSupport.distinctUniversities(List.of(historySignals.latestUniversity(), currentUniversities.get(0)));
            }
            return GraduateKnowledgeResolutionSupport.distinctUniversities(currentUniversities);
        }

        if (containsAny(normalizedMessage, "cheaper", "cheapest", "same", "same at", "is it the same", "compare", "comparison")
                && historySignals.distinctUniversityCount() == 2) {
            return historySignals.distinctUniversities();
        }

        if (historySignals.latestUniversity() != null && isFollowUpMessage(normalizedMessage)) {
            return List.of(historySignals.latestUniversity());
        }

        return List.of();
    }

    private List<String> resolveDegreeTypesForFollowUp(List<String> currentDegreeTypes, GraduateKnowledgeResolutionSupport.HistorySignals historySignals) {
        if (!currentDegreeTypes.isEmpty()) {
            return currentDegreeTypes;
        }
        if (historySignals.latestDegreeType() != null) {
            return List.of(historySignals.latestDegreeType());
        }
        return List.of();
    }

    private boolean shouldFlagAmbiguousProgram(
            String normalizedMessage,
            List<ResolvedUniversity> currentUniversities,
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            boolean explicitComparison,
            List<ResolvedUniversity> resolvedUniversities
    ) {
        if (!resolvedUniversities.isEmpty()) {
            return false;
        }
        if (currentUniversities.isEmpty() && historySignals.hasMultipleDistinctUniversities() && isFollowUpMessage(normalizedMessage) && !explicitComparison) {
            return true;
        }
        return currentUniversities.isEmpty();
    }

    private boolean shouldFlagAmbiguousTuition(
            String normalizedMessage,
            List<ResolvedUniversity> currentUniversities,
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            boolean explicitComparison,
            List<ResolvedUniversity> resolvedUniversities
    ) {
        if (!resolvedUniversities.isEmpty()) {
            return false;
        }
        if (currentUniversities.isEmpty() && historySignals.hasMultipleDistinctUniversities() && isFollowUpMessage(normalizedMessage) && !explicitComparison) {
            return true;
        }
        return currentUniversities.isEmpty();
    }

    private boolean shouldFlagAmbiguousBranch(
            List<ResolvedUniversity> currentUniversities,
            GraduateKnowledgeResolutionSupport.HistorySignals historySignals,
            boolean explicitComparison,
            List<ResolvedUniversity> resolvedUniversities
    ) {
        if (!resolvedUniversities.isEmpty()) {
            return false;
        }
        if (currentUniversities.isEmpty() && historySignals.hasMultipleDistinctUniversities() && explicitComparison) {
            return true;
        }
        return currentUniversities.isEmpty();
    }

    private GraduateKnowledgeIntent latestIntentCue(String normalizedMessage) {
        if (GraduateKnowledgeResolutionSupport.detectLocationLookupIntent(normalizedMessage)) {
            return GraduateKnowledgeIntent.LOCATION_LOOKUP;
        }
        if (GraduateKnowledgeResolutionSupport.detectAcademicStructureLookupIntent(normalizedMessage)) {
            return GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP;
        }
        boolean tuitionIntent = detectTuitionAggregationIntent(normalizedMessage);
        boolean programIntent = detectProgramLookupIntent(normalizedMessage, detectDegreeTypes(normalizedMessage), tuitionIntent);
        if (GraduateKnowledgeResolutionSupport.detectGraduateOverviewIntent(normalizedMessage, programIntent, tuitionIntent)) {
            return GraduateKnowledgeIntent.GRADUATE_OVERVIEW;
        }
        if (tuitionIntent) {
            return GraduateKnowledgeIntent.TUITION_AGGREGATION;
        }
        if (programIntent) {
            return GraduateKnowledgeIntent.PROGRAM_LOOKUP;
        }
        return GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
    }

    private GraduateKnowledgeResolutionSupport.HistorySignals analyzeHistory(List<AiConversationMessage> recentConversationHistory, List<UniversityCatalog> catalogs, ConversationMemory conversationMemory) {
        return GraduateKnowledgeResolutionSupport.analyzeHistorySignals(recentConversationHistory, catalogs, conversationMemory);
    }

    private List<ResolvedUniversity> resolveUniversities(String normalizedMessage, List<UniversityCatalog> catalogs) {
        return GraduateKnowledgeResolutionSupport.resolveUniversities(normalizedMessage, catalogs);
    }

    private List<String> detectDegreeTypes(String normalizedMessage) {
        return GraduateKnowledgeResolutionSupport.detectDegreeTypes(normalizedMessage);
    }

    private boolean detectProgramLookupIntent(String normalizedMessage, List<String> degreeTypes, boolean tuitionIntent) {
        return GraduateKnowledgeResolutionSupport.detectProgramLookupIntent(normalizedMessage, degreeTypes, tuitionIntent);
    }

    private boolean detectTuitionAggregationIntent(String normalizedMessage) {
        return GraduateKnowledgeResolutionSupport.detectTuitionAggregationIntent(normalizedMessage);
    }

    private boolean containsAny(String text, String... phrases) {
        return GraduateKnowledgeResolutionSupport.containsAny(text, phrases);
    }

    private boolean isFollowUpMessage(String normalizedMessage) {
        return GraduateKnowledgeResolutionSupport.isFollowUpMessage(normalizedMessage);
    }

    public static boolean isDeterministicGeneralChatMessage(String message) {
        String normalized = message == null ? "" : message.trim().toLowerCase().replaceAll("[!?.,]+$", "");
        return switch (normalized) {
            case "hi", "hello", "hey", "good morning", "good afternoon", "good evening",
                    "thanks", "thank you", "bye", "goodbye", "how are you", "what can you do", "who are you" -> true;
            default -> false;
        };
    }

    public boolean hasGraduateSignal(
            String userMessage,
            List<AiConversationMessage> recentConversationHistory,
            List<UniversityCatalog> universityCatalogs,
            ConversationMemory conversationMemory
    ) {
        String normalizedMessage = normalize(userMessage);
        List<UniversityCatalog> catalog = universityCatalogs == null ? List.of() : List.copyOf(universityCatalogs);
        List<String> degreeTypes = detectDegreeTypes(normalizedMessage);
        boolean tuitionIntent = detectTuitionAggregationIntent(normalizedMessage);
        boolean programIntent = detectProgramLookupIntent(normalizedMessage, degreeTypes, tuitionIntent);
        if (!resolveUniversities(normalizedMessage, catalog).isEmpty()
                || !degreeTypes.isEmpty()
                || tuitionIntent
                || programIntent
                || GraduateKnowledgeResolutionSupport.detectGraduateOverviewIntent(normalizedMessage, programIntent, tuitionIntent)
                || GraduateKnowledgeResolutionSupport.detectLocationLookupIntent(normalizedMessage)
                || GraduateKnowledgeResolutionSupport.detectAcademicStructureLookupIntent(normalizedMessage)
                || containsAny(normalizedMessage, "university", "college", "institute", "school", "faculty", "department")
                || containsAny(normalizedMessage, "bachelor", "undergraduate", "undergrad")) {
            return true;
        }

        if (!isFollowUpMessage(normalizedMessage)) {
            return false;
        }
        GraduateKnowledgeResolutionSupport.HistorySignals historySignals = analyzeHistory(
                recentConversationHistory,
                catalog,
                conversationMemory
        );
        return historySignals.latestIntent() != GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS
                || (conversationMemory != null && conversationMemory.lastIntentEnum() != GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS);
    }

    /**
     * Conservative confidence gate for the deterministic-first orchestration path.
     * Complex, comparative, follow-up, unresolved, and ambiguous requests remain provider eligible.
     */
    public boolean isHighConfidenceDeterministic(
            String userMessage,
            GraduateKnowledgeQuery query
    ) {
        if (query == null || query.ambiguous()
                || query.intent() == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS
                || query.intent() == GraduateKnowledgeIntent.GENERAL_CHAT
                || query.operation() == GraduateKnowledgeOperation.COMPARE) {
            return false;
        }
        String normalized = normalize(userMessage);
        if (normalized.isBlank()
                || GraduateKnowledgeResolutionSupport.isSubjectiveComparison(normalized)
                || isFollowUpMessage(normalized)
                || containsAny(normalized, "compare", "comparison", " vs ", "versus", "between", "which one", "more ", "cheaper", "best", "better")) {
            return false;
        }
        boolean locationCue = containsAny(normalized, "campus", "campuses", "university", "universities", "located", "how many", "number of");
        boolean academicCue = containsAny(normalized, "faculty", "faculties", "department", "departments", "admission", "admissions", "language", "languages");
        boolean tuitionCue = detectTuitionAggregationIntent(normalized);
        boolean recognizedHighConfidenceResource = (query.resource() == GraduateKnowledgeResource.CAMPUS
                || query.resource() == GraduateKnowledgeResource.UNIVERSITY) && locationCue
                || (query.resource() == GraduateKnowledgeResource.FACULTY
                || query.resource() == GraduateKnowledgeResource.DEPARTMENT) && academicCue
                || query.operation() == GraduateKnowledgeOperation.AGGREGATE && tuitionCue;
        if (!recognizedHighConfidenceResource) {
            return false;
        }
        boolean globalCount = query.operation() == GraduateKnowledgeOperation.COUNT
                && (query.resource() == GraduateKnowledgeResource.UNIVERSITY
                || query.resource() == GraduateKnowledgeResource.CAMPUS)
                && query.resolvedUniversities().isEmpty()
                && query.filters().city() == null;
        boolean scopedDecision = !query.resolvedUniversities().isEmpty()
                || query.filters().city() != null;
        return globalCount || scopedDecision;
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }

}
