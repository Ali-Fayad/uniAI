package com.uniai.chat.application.retrieval;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.catalog.domain.model.UniversityCatalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GraduateKnowledgeQueryInterpreter {

    private static final int MAX_RECENT_HISTORY_MESSAGES = 6;
    private static final Pattern WORD_SPLIT = Pattern.compile("[^A-Za-z0-9+]+");

    public GraduateKnowledgeQuery interpret(
            String userMessage,
            List<AiConversationMessage> recentConversationHistory,
            List<UniversityCatalog> universityCatalogs
    ) {
        String normalizedMessage = normalize(userMessage);
        List<UniversityCatalog> catalog = universityCatalogs == null ? List.of() : List.copyOf(universityCatalogs);
        HistorySignals historySignals = analyzeHistory(recentConversationHistory, catalog);

        List<ResolvedUniversity> currentUniversities = resolveUniversities(normalizedMessage, catalog);
        List<String> currentDegreeTypes = detectDegreeTypes(normalizedMessage);
        boolean currentTuitionIntent = detectTuitionAggregationIntent(normalizedMessage);
        boolean currentProgramIntent = detectProgramLookupIntent(normalizedMessage, currentDegreeTypes, currentTuitionIntent);
        boolean currentFollowUp = isFollowUpMessage(normalizedMessage);
        boolean explicitComparison = containsAny(normalizedMessage, "compare", "comparison", "vs", "versus", "between", "with");

        GraduateKnowledgeIntent intent = GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
        GraduateProgramDetailLevel detailLevel = GraduateProgramDetailLevel.LIST;

        boolean followUpResolved = false;
        boolean ambiguous = false;
        List<ResolvedUniversity> resolvedUniversities = new ArrayList<>();
        List<String> resolvedDegreeTypes = new ArrayList<>();

        GraduateKnowledgeIntent inheritedHistoryIntent = historySignals.latestIntent();

        if (currentTuitionIntent
                || (currentFollowUp && inheritedHistoryIntent == GraduateKnowledgeIntent.TUITION_AGGREGATION)
                || (explicitComparison && inheritedHistoryIntent == GraduateKnowledgeIntent.TUITION_AGGREGATION && currentUniversities.isEmpty())) {
            intent = GraduateKnowledgeIntent.TUITION_AGGREGATION;
            followUpResolved = currentFollowUp;
            resolvedUniversities = resolveTuitionUniversities(normalizedMessage, currentUniversities, historySignals, explicitComparison);
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals, true);
            ambiguous = shouldFlagAmbiguousTuition(normalizedMessage, currentUniversities, historySignals, explicitComparison, resolvedUniversities);
        } else if (currentProgramIntent
                || (currentFollowUp && inheritedHistoryIntent == GraduateKnowledgeIntent.PROGRAM_LOOKUP)
                || (!currentUniversities.isEmpty() && currentFollowUp)) {
            intent = GraduateKnowledgeIntent.PROGRAM_LOOKUP;
            followUpResolved = currentFollowUp;
            resolvedUniversities = resolveProgramUniversities(normalizedMessage, currentUniversities, historySignals, explicitComparison);
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals, false);
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
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals, intent == GraduateKnowledgeIntent.TUITION_AGGREGATION);
            if (intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP) {
                detailLevel = resolveDetailLevel(normalizedMessage);
            }
            ambiguous = shouldFlagAmbiguousBranch(currentUniversities, historySignals, explicitComparison, resolvedUniversities);
        } else if (!currentUniversities.isEmpty() && !currentDegreeTypes.isEmpty()) {
            intent = GraduateKnowledgeIntent.PROGRAM_LOOKUP;
            resolvedUniversities = distinctUniversities(currentUniversities);
            resolvedDegreeTypes = new ArrayList<>(currentDegreeTypes);
            detailLevel = resolveDetailLevel(normalizedMessage);
        } else if (historySignals.canInheritProgramIntent(currentFollowUp, normalizedMessage)) {
            intent = GraduateKnowledgeIntent.PROGRAM_LOOKUP;
            followUpResolved = true;
            resolvedUniversities = historySignals.latestUniversity() == null ? List.of() : List.of(historySignals.latestUniversity());
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals, false);
            detailLevel = resolveDetailLevel(normalizedMessage);
            ambiguous = resolvedUniversities.isEmpty();
        } else if (historySignals.canInheritTuitionIntent(currentFollowUp, normalizedMessage)) {
            intent = GraduateKnowledgeIntent.TUITION_AGGREGATION;
            followUpResolved = true;
            resolvedUniversities = historySignals.latestUniversity() == null ? List.of() : List.of(historySignals.latestUniversity());
            resolvedDegreeTypes = resolveDegreeTypesForFollowUp(currentDegreeTypes, historySignals, true);
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

        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION && historySignals.hasMultipleDistinctUniversities() && currentUniversities.isEmpty() && !explicitComparison) {
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
            HistorySignals historySignals,
            boolean explicitComparison
    ) {
        if (!currentUniversities.isEmpty()) {
            if (explicitComparison && currentUniversities.size() == 1 && historySignals.latestUniversity() != null
                    && !sameUniversity(historySignals.latestUniversity(), currentUniversities.get(0))) {
                return distinctUniversities(List.of(historySignals.latestUniversity(), currentUniversities.get(0)));
            }
            return distinctUniversities(currentUniversities);
        }

        if (historySignals.latestUniversity() != null && isFollowUpMessage(normalizedMessage)) {
            return List.of(historySignals.latestUniversity());
        }

        return List.of();
    }

    private List<ResolvedUniversity> resolveTuitionUniversities(
            String normalizedMessage,
            List<ResolvedUniversity> currentUniversities,
            HistorySignals historySignals,
            boolean explicitComparison
    ) {
        if (!currentUniversities.isEmpty()) {
            if (explicitComparison && currentUniversities.size() == 1 && historySignals.latestUniversity() != null
                    && !sameUniversity(historySignals.latestUniversity(), currentUniversities.get(0))) {
                return distinctUniversities(List.of(historySignals.latestUniversity(), currentUniversities.get(0)));
            }
            return distinctUniversities(currentUniversities);
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

    private List<String> resolveDegreeTypesForFollowUp(List<String> currentDegreeTypes, HistorySignals historySignals, boolean tuitionIntent) {
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
            HistorySignals historySignals,
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
            HistorySignals historySignals,
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
            HistorySignals historySignals,
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
        if (detectTuitionAggregationIntent(normalizedMessage)) {
            return GraduateKnowledgeIntent.TUITION_AGGREGATION;
        }
        if (detectProgramLookupIntent(normalizedMessage, detectDegreeTypes(normalizedMessage), false)) {
            return GraduateKnowledgeIntent.PROGRAM_LOOKUP;
        }
        return GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
    }

    private HistorySignals analyzeHistory(List<AiConversationMessage> recentConversationHistory, List<UniversityCatalog> catalogs) {
        if (recentConversationHistory == null || recentConversationHistory.isEmpty()) {
            return HistorySignals.empty();
        }

        int startIndex = Math.max(0, recentConversationHistory.size() - MAX_RECENT_HISTORY_MESSAGES);
        List<AiConversationMessage> window = recentConversationHistory.subList(startIndex, recentConversationHistory.size());

        List<ResolvedUniversity> distinctUniversities = new ArrayList<>();
        ResolvedUniversity latestUniversity = null;
        String latestDegreeType = null;
        GraduateKnowledgeIntent latestIntent = GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;

        for (AiConversationMessage message : window) {
            if (message == null || message.getContent() == null || message.getContent().isBlank()) {
                continue;
            }
            String normalized = normalize(message.getContent());
            List<ResolvedUniversity> matches = resolveUniversities(normalized, catalogs);
            if (!matches.isEmpty()) {
                latestUniversity = matches.get(matches.size() - 1);
                addDistinctUniversities(distinctUniversities, matches);
            }

            List<String> degreeTypes = detectDegreeTypes(normalized);
            if (!degreeTypes.isEmpty()) {
                latestDegreeType = degreeTypes.get(degreeTypes.size() - 1);
            }

            GraduateKnowledgeIntent candidateIntent = latestIntentCue(normalized);
            if (candidateIntent != GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
                latestIntent = candidateIntent;
            }
        }

        return new HistorySignals(distinctUniversities, latestUniversity, latestDegreeType, latestIntent);
    }

    private List<ResolvedUniversity> resolveUniversities(String normalizedMessage, List<UniversityCatalog> catalogs) {
        if (normalizedMessage == null || normalizedMessage.isBlank() || catalogs == null || catalogs.isEmpty()) {
            return List.of();
        }

        Map<Long, ResolvedUniversity> resolved = new LinkedHashMap<>();
        for (UniversityCatalog catalog : catalogs) {
            if (catalog == null) {
                continue;
            }
            if (matchesUniversity(normalizedMessage, catalog)) {
                resolved.putIfAbsent(catalog.getId(), new ResolvedUniversity(catalog.getId(), catalog.getName(), catalog.getAcronym()));
            }
        }
        return new ArrayList<>(resolved.values());
    }

    private List<String> detectDegreeTypes(String normalizedMessage) {
        if (normalizedMessage == null || normalizedMessage.isBlank()) {
            return List.of();
        }

        Set<String> degreeTypes = new LinkedHashSet<>();
        if (matchesMaster(normalizedMessage)) {
            degreeTypes.add("MASTER");
        }
        if (matchesPhd(normalizedMessage)) {
            degreeTypes.add("PHD");
        }
        return new ArrayList<>(degreeTypes);
    }

    private boolean detectProgramLookupIntent(String normalizedMessage, List<String> degreeTypes, boolean tuitionIntent) {
        if (tuitionIntent) {
            return false;
        }
        if (!degreeTypes.isEmpty()) {
            return true;
        }
        return containsAny(normalizedMessage,
                "program",
                "programs",
                "graduate",
                "degree",
                "degrees",
                "available",
                "offer",
                "offers",
                "details",
                "detail",
                "admission",
                "requirements",
                "language",
                "credits",
                "duration",
                "thesis",
                "tracks");
    }

    private boolean detectTuitionAggregationIntent(String normalizedMessage) {
        if (normalizedMessage == null || normalizedMessage.isBlank()) {
            return false;
        }

        return containsAny(normalizedMessage,
                "tuition",
                "fee",
                "fees",
                "cost",
                "cheaper",
                "cheapest",
                "average tuition",
                "avg tuition",
                "mean tuition",
                "same tuition",
                "compare tuition",
                "tuition comparison",
                "tuition average",
                "tuition cost")
                || (normalizedMessage.contains("tuition")
                && containsAny(normalizedMessage, "average", "avg", "mean", "compare", "comparison", "same", "cheaper", "cost"));
    }

    private boolean matchesMaster(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalizedMessage = text.toLowerCase(Locale.ROOT);
        return containsAny(normalizedMessage, "master", "masters", "master's", "mba", "m.a.", "m.sc")
                || containsWord(normalizedMessage, "ms");
    }

    private boolean matchesPhd(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalizedMessage = text.toLowerCase(Locale.ROOT);
        return normalizedMessage.contains("phd")
                || normalizedMessage.contains("doctor of philosophy")
                || normalizedMessage.contains("doctoral")
                || normalizedMessage.contains("doctorate");
    }

    private boolean matchesUniversity(String text, UniversityCatalog university) {
        if (text == null || text.isBlank() || university == null) {
            return false;
        }
        String normalizedText = text.toLowerCase(Locale.ROOT);

        if (university.getAcronym() != null && !university.getAcronym().isBlank()) {
            String acronym = university.getAcronym().toLowerCase(Locale.ROOT);
            if (containsWord(normalizedText, acronym)) {
                return true;
            }
        }

        if (university.getName() != null && !university.getName().isBlank()) {
            String name = university.getName().toLowerCase(Locale.ROOT);
            if (normalizedText.contains(name)) {
                return true;
            }

            for (String token : tokenize(name)) {
                if (token.length() > 2 && containsWord(normalizedText, token)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsAny(String text, String... phrases) {
        if (text == null || text.isBlank() || phrases == null) {
            return false;
        }
        String normalizedText = text.toLowerCase(Locale.ROOT);
        for (String phrase : phrases) {
            if (phrase != null && !phrase.isBlank() && normalizedText.contains(phrase.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean isFollowUpMessage(String normalizedMessage) {
        return containsAny(normalizedMessage,
                "same",
                "same at",
                "what about",
                "how about",
                "and for",
                "also",
                "there",
                "it",
                "compare",
                "comparison",
                "vs",
                "versus",
                "between");
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsWord(String haystack, String needle) {
        if (haystack == null || haystack.isBlank() || needle == null || needle.isBlank()) {
            return false;
        }
        String[] tokens = WORD_SPLIT.split(haystack);
        for (String token : tokens) {
            if (token.equalsIgnoreCase(needle)) {
                return true;
            }
        }
        return false;
    }

    private List<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        for (String token : WORD_SPLIT.split(value)) {
            if (token != null && !token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private void addDistinctUniversities(List<ResolvedUniversity> target, List<ResolvedUniversity> matches) {
        Map<Long, ResolvedUniversity> byId = target.stream()
                .filter(university -> university != null && university.id() != null)
                .collect(Collectors.toMap(ResolvedUniversity::id, university -> university, (left, right) -> left, LinkedHashMap::new));
        for (ResolvedUniversity university : matches) {
            if (university == null || university.id() == null) {
                continue;
            }
            byId.putIfAbsent(university.id(), university);
        }
        target.clear();
        target.addAll(byId.values());
    }

    private List<ResolvedUniversity> distinctUniversities(List<ResolvedUniversity> universities) {
        if (universities == null || universities.isEmpty()) {
            return List.of();
        }
        Map<Long, ResolvedUniversity> byId = new LinkedHashMap<>();
        for (ResolvedUniversity university : universities) {
            if (university == null || university.id() == null) {
                continue;
            }
            byId.putIfAbsent(university.id(), university);
        }
        return new ArrayList<>(byId.values());
    }

    private boolean sameUniversity(ResolvedUniversity left, ResolvedUniversity right) {
        return left != null && right != null && Objects.equals(left.id(), right.id());
    }

    private record HistorySignals(
            List<ResolvedUniversity> distinctUniversities,
            ResolvedUniversity latestUniversity,
            String latestDegreeType,
            GraduateKnowledgeIntent latestIntent
    ) {
        private static HistorySignals empty() {
            return new HistorySignals(List.of(), null, null, GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS);
        }

        private boolean hasMultipleDistinctUniversities() {
            return distinctUniversities != null && distinctUniversities.size() > 1;
        }

        private int distinctUniversityCount() {
            return distinctUniversities == null ? 0 : distinctUniversities.size();
        }

        private boolean canInheritProgramIntent(boolean currentFollowUp, String normalizedMessage) {
            return currentFollowUp && latestIntent == GraduateKnowledgeIntent.PROGRAM_LOOKUP;
        }

        private boolean canInheritTuitionIntent(boolean currentFollowUp, String normalizedMessage) {
            return currentFollowUp && latestIntent == GraduateKnowledgeIntent.TUITION_AGGREGATION;
        }
    }
}
