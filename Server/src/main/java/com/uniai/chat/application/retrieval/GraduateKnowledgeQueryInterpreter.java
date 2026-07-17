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
        GraduateKnowledgeResolutionSupport.HistorySignals historySignals = analyzeHistory(recentConversationHistory, catalog, conversationMemory);

        List<ResolvedUniversity> currentUniversities = resolveUniversities(normalizedMessage, catalog);
        List<String> currentDegreeTypes = detectDegreeTypes(normalizedMessage);
        boolean currentTuitionIntent = detectTuitionAggregationIntent(normalizedMessage);
        boolean currentProgramIntent = detectProgramLookupIntent(normalizedMessage, currentDegreeTypes, currentTuitionIntent);
        boolean currentOverviewIntent = GraduateKnowledgeResolutionSupport.detectGraduateOverviewIntent(normalizedMessage, currentProgramIntent, currentTuitionIntent);
        boolean currentFollowUp = isFollowUpMessage(normalizedMessage);
        boolean explicitComparison = containsAny(normalizedMessage, "compare", "comparison", "vs", "versus", "between", "with");

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

        if (currentOverviewIntent
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
        } else if (currentProgramIntent
                || (currentFollowUp && inheritedHistoryIntent == GraduateKnowledgeIntent.PROGRAM_LOOKUP)
                || (!currentUniversities.isEmpty() && currentFollowUp)) {
            intent = GraduateKnowledgeIntent.PROGRAM_LOOKUP;
            followUpResolved = currentFollowUp;
            resolvedUniversities = resolveProgramUniversities(normalizedMessage, currentUniversities, historySignals, explicitComparison);
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals);
            detailLevel = resolveDetailLevel(normalizedMessage);
            ambiguous = shouldFlagAmbiguousProgram(normalizedMessage, currentUniversities, historySignals, explicitComparison, resolvedUniversities);
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
            resolvedUniversities = historySignals.latestUniversity() == null ? List.of() : List.of(historySignals.latestUniversity());
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

        if (resolvedUniversities.isEmpty()) {
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

        return new GraduateKnowledgeQuery(
                intent,
                resolvedUniversities,
                resolvedDegreeTypes,
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

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }

}
