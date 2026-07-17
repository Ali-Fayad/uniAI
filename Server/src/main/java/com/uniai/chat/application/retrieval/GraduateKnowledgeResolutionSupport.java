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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class GraduateKnowledgeResolutionSupport {

    private static final Pattern WORD_SPLIT = Pattern.compile("[^A-Za-z0-9+]+");
    private static final Pattern CITY_AFTER_IN = Pattern.compile("\\bin\\s+([\\p{L}][\\p{L}\\s'\\-]{1,60}?)(?:[?!.;,]|$)", Pattern.CASE_INSENSITIVE);
    private static final Set<String> GENERIC_UNIVERSITY_TOKENS = Set.of(
            "university",
            "college",
            "institute",
            "school",
            "faculty",
            "department"
    );

    private GraduateKnowledgeResolutionSupport() {
    }

    static List<ResolvedUniversity> resolveUniversities(String text, List<UniversityCatalog> catalogs) {
        if (text == null || text.isBlank() || catalogs == null || catalogs.isEmpty()) {
            return List.of();
        }

        String normalizedText = normalize(text);
        Map<Long, ResolvedUniversity> resolved = new LinkedHashMap<>();
        for (UniversityCatalog catalog : catalogs) {
            if (catalog == null || catalog.getId() == null) {
                continue;
            }
            if (matchesUniversity(normalizedText, catalog)) {
                resolved.putIfAbsent(catalog.getId(), new ResolvedUniversity(catalog.getId(), catalog.getName(), catalog.getAcronym()));
            }
        }
        return new ArrayList<>(resolved.values());
    }

    static List<String> detectDegreeTypes(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = normalize(text);
        Set<String> degreeTypes = new LinkedHashSet<>();
        if (containsAny(normalized, "master", "masters", "master's", "mba", "m.a.", "m.sc") || containsWord(normalized, "ms")) {
            degreeTypes.add("MASTER");
        }
        if (normalized.contains("phd")
                || normalized.contains("doctor of philosophy")
                || normalized.contains("doctoral")
                || normalized.contains("doctorate")) {
            degreeTypes.add("PHD");
        }
        return new ArrayList<>(degreeTypes);
    }

    static boolean detectTuitionAggregationIntent(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized = normalize(text);
        return containsAny(normalized,
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
                || (normalized.contains("tuition")
                && containsAny(normalized, "average", "avg", "mean", "compare", "comparison", "same", "cheaper", "cost"));
    }

    static boolean detectProgramLookupIntent(String text, List<String> degreeTypes, boolean tuitionIntent) {
        if (tuitionIntent) {
            return false;
        }
        if (degreeTypes != null && !degreeTypes.isEmpty()) {
            return true;
        }
        String normalized = normalize(text);
        return containsAny(normalized,
                "program",
                "programs",
                "برنامج",
                "برامج",
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

    static boolean detectLocationLookupIntent(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = normalize(text);
        return containsAny(normalized,
                "campus", "campuses", "campus in", "located in",
                "which universities are in", "which university is in",
                "universities in", "university in", "universities located in",
                "university located in");
    }

    static GraduateKnowledgeResource detectLocationResource(String text) {
        return containsAny(normalize(text), "campus", "campuses")
                ? GraduateKnowledgeResource.CAMPUS
                : GraduateKnowledgeResource.UNIVERSITY;
    }

    static GraduateKnowledgeOperation detectLocationOperation(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "how many", "count", "number of")) {
            return GraduateKnowledgeOperation.COUNT;
        }
        if (containsAny(normalized, "does ", "do ", "is there", "are there", "has ", "have ", "exist")) {
            return GraduateKnowledgeOperation.EXISTS;
        }
        return GraduateKnowledgeOperation.LIST;
    }

    static String detectStructuredCity(String text, List<UniversityCatalog> catalogs) {
        if (text == null || text.isBlank() || catalogs == null) {
            return null;
        }
        String normalized = normalize(text);
        return catalogs.stream()
                .map(UniversityCatalog::getCity)
                .filter(city -> city != null && !city.isBlank())
                .distinct()
                .sorted((left, right) -> Integer.compare(right.length(), left.length()))
                .filter(city -> normalized.contains(normalize(city)))
                .findFirst()
                .orElse(null);
    }

    static String detectRequestedCity(String text, List<UniversityCatalog> catalogs) {
        String structuredCity = detectStructuredCity(text, catalogs);
        if (structuredCity != null) {
            return structuredCity;
        }
        if (text == null || text.isBlank()) {
            return null;
        }
        java.util.regex.Matcher matcher = CITY_AFTER_IN.matcher(text.trim());
        if (!matcher.find()) {
            return null;
        }
        String city = matcher.group(1).trim();
        return city.isBlank() ? null : city;
    }

    static boolean isFollowUpMessage(String text) {
        return containsStandaloneTokenOrPhrase(text,
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

    static boolean isCorrectionMessage(String text) {
        String normalized = normalize(text);
        return containsAny(normalized,
                "no, i meant",
                "actually",
                "instead",
                "sorry, i meant",
                "rather",
                "change that to",
                "not aub",
                "not usj",
                "not lau");
    }

    static boolean containsStandaloneTokenOrPhrase(String text, String... phrases) {
        if (text == null || text.isBlank() || phrases == null || phrases.length == 0) {
            return false;
        }

        List<String> tokens = tokenize(normalize(text));
        if (tokens.isEmpty()) {
            return false;
        }

        for (String phrase : phrases) {
            if (phrase == null || phrase.isBlank()) {
                continue;
            }
            List<String> phraseTokens = tokenize(normalize(phrase));
            if (phraseTokens.isEmpty()) {
                continue;
            }
            if (phraseTokens.size() == 1) {
                if (tokens.contains(phraseTokens.get(0))) {
                    return true;
                }
                continue;
            }
            if (containsTokenSequence(tokens, phraseTokens)) {
                return true;
            }
        }
        return false;
    }

    static boolean containsAny(String text, String... phrases) {
        if (text == null || text.isBlank() || phrases == null) {
            return false;
        }
        String normalizedText = normalize(text);
        for (String phrase : phrases) {
            if (phrase != null && !phrase.isBlank() && normalizedText.contains(phrase.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    static boolean sameUniversity(ResolvedUniversity left, ResolvedUniversity right) {
        return left != null && right != null && Objects.equals(left.id(), right.id());
    }

    static List<ResolvedUniversity> distinctUniversities(List<ResolvedUniversity> universities) {
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

    static List<ResolvedUniversity> mergeUniversities(List<ResolvedUniversity> left, List<ResolvedUniversity> right) {
        Map<Long, ResolvedUniversity> byId = new LinkedHashMap<>();
        if (left != null) {
            for (ResolvedUniversity university : left) {
                if (university != null && university.id() != null) {
                    byId.putIfAbsent(university.id(), university);
                }
            }
        }
        if (right != null) {
            for (ResolvedUniversity university : right) {
                if (university != null && university.id() != null) {
                    byId.putIfAbsent(university.id(), university);
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    static List<MemoryUniversityRef> asMemoryRefs(List<ResolvedUniversity> universities) {
        if (universities == null || universities.isEmpty()) {
            return List.of();
        }
        List<MemoryUniversityRef> refs = new ArrayList<>();
        for (ResolvedUniversity university : universities) {
            if (university != null && university.id() != null) {
                refs.add(new MemoryUniversityRef(university.id(), university.name(), university.acronym()));
            }
        }
        return refs;
    }

    static HistorySignals analyzeHistorySignals(
            List<AiConversationMessage> recentConversationHistory,
            List<UniversityCatalog> catalogs,
            ConversationMemory conversationMemory
    ) {
        if (recentConversationHistory == null || recentConversationHistory.isEmpty()) {
            return seedFromMemory(HistorySignals.empty(), conversationMemory);
        }

        int startIndex = Math.max(0, recentConversationHistory.size() - 6);
        List<AiConversationMessage> window = recentConversationHistory.subList(startIndex, recentConversationHistory.size());

        List<ResolvedUniversity> distinctUniversities = new ArrayList<>();
        ResolvedUniversity latestUniversity = null;
        String latestDegreeType = null;
        GraduateKnowledgeIntent latestIntent = GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;

        for (AiConversationMessage message : window) {
            if (!isUserRole(message) || message.getContent() == null || message.getContent().isBlank()) {
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

        return seedFromMemory(new HistorySignals(distinctUniversities, latestUniversity, latestDegreeType, latestIntent), conversationMemory);
    }

    private static boolean isUserRole(AiConversationMessage message) {
        if (message == null || message.getRole() == null) {
            return false;
        }
        return "user".equalsIgnoreCase(message.getRole());
    }

    private static HistorySignals seedFromMemory(
            HistorySignals base,
            ConversationMemory conversationMemory
    ) {
        if (conversationMemory == null || conversationMemory.isEmpty()) {
            return base;
        }

        List<ResolvedUniversity> memoryUniversities = asResolvedUniversities(conversationMemory.activeUniversities());
        List<ResolvedUniversity> distinctUniversities = base.distinctUniversities();
        if (memoryUniversities != null && !memoryUniversities.isEmpty()) {
            distinctUniversities = mergeUniversities(memoryUniversities, distinctUniversities);
        }

        ResolvedUniversity latestUniversity = base.latestUniversity();
        if (latestUniversity == null && memoryUniversities != null && !memoryUniversities.isEmpty()) {
            latestUniversity = memoryUniversities.get(memoryUniversities.size() - 1);
        }

        String latestDegreeType = base.latestDegreeType();
        if (latestDegreeType == null && conversationMemory.activeDegreeTypes() != null && !conversationMemory.activeDegreeTypes().isEmpty()) {
            latestDegreeType = conversationMemory.activeDegreeTypes().get(conversationMemory.activeDegreeTypes().size() - 1);
        }

        GraduateKnowledgeIntent latestIntent = base.latestIntent();
        if (latestIntent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            latestIntent = conversationMemory.lastIntentEnum();
        }

        return new HistorySignals(distinctUniversities, latestUniversity, latestDegreeType, latestIntent);
    }

    private static List<ResolvedUniversity> asResolvedUniversities(List<MemoryUniversityRef> refs) {
        if (refs == null || refs.isEmpty()) {
            return List.of();
        }
        List<ResolvedUniversity> universities = new ArrayList<>();
        for (MemoryUniversityRef ref : refs) {
            if (ref != null && ref.id() != null) {
                universities.add(new ResolvedUniversity(ref.id(), ref.name(), ref.acronym()));
            }
        }
        return universities;
    }

    private static GraduateKnowledgeIntent latestIntentCue(String normalizedMessage) {
        if (detectLocationLookupIntent(normalizedMessage)) {
            return GraduateKnowledgeIntent.LOCATION_LOOKUP;
        }
        boolean tuitionIntent = detectTuitionAggregationIntent(normalizedMessage);
        boolean programIntent = detectProgramLookupIntent(normalizedMessage, detectDegreeTypes(normalizedMessage), tuitionIntent);
        if (detectGraduateOverviewIntent(normalizedMessage, programIntent, tuitionIntent)) {
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

    static boolean detectGraduateOverviewIntent(String text, boolean currentProgramIntent, boolean currentTuitionIntent) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized = normalize(text);
        if (containsStandaloneTokenOrPhrase(normalized, "both", "both please")) {
            return true;
        }
        if (containsAny(normalized, "tuition and programs", "programs and tuition")) {
            return true;
        }
        if (currentProgramIntent || currentTuitionIntent) {
            return false;
        }
        return containsAny(normalized,
                "what do you know about",
                "what can you tell me about",
                "tell me about",
                "give me an overview",
                "overview of",
                "graduate overview",
                "overview");
    }

    private static boolean matchesUniversity(String normalizedText, UniversityCatalog university) {
        if (normalizedText == null || normalizedText.isBlank() || university == null) {
            return false;
        }

        if (university.getAcronym() != null && !university.getAcronym().isBlank()) {
            String acronym = university.getAcronym().toLowerCase(Locale.ROOT);
            if (containsWord(normalizedText, acronym)) {
                return true;
            }
        }

        if (university.getName() != null && !university.getName().isBlank()) {
            String name = normalize(university.getName());
            if (normalizedText.contains(name)) {
                return true;
            }

            for (String token : tokenize(name)) {
                if (token.length() > 2 && !GENERIC_UNIVERSITY_TOKENS.contains(token) && containsWord(normalizedText, token)) {
                    return true;
                }
            }
        }

        if (university.getNameAr() != null && !university.getNameAr().isBlank()) {
            String nameAr = normalize(university.getNameAr());
            if (normalizedText.contains(nameAr)) {
                return true;
            }
        }

        return false;
    }

    private static List<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        for (String token : WORD_SPLIT.split(value)) {
            if (token != null && !token.isBlank()) {
                tokens.add(token.toLowerCase(Locale.ROOT));
            }
        }
        return tokens;
    }

    private static boolean containsTokenSequence(List<String> tokens, List<String> phraseTokens) {
        if (tokens == null || phraseTokens == null || tokens.size() < phraseTokens.size()) {
            return false;
        }
        for (int index = 0; index <= tokens.size() - phraseTokens.size(); index++) {
            boolean matches = true;
            for (int offset = 0; offset < phraseTokens.size(); offset++) {
                if (!tokens.get(index + offset).equals(phraseTokens.get(offset))) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean containsWord(String haystack, String needle) {
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

    private static void addDistinctUniversities(List<ResolvedUniversity> target, List<ResolvedUniversity> matches) {
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

    record HistorySignals(
            List<ResolvedUniversity> distinctUniversities,
            ResolvedUniversity latestUniversity,
            String latestDegreeType,
            GraduateKnowledgeIntent latestIntent
    ) {
        static HistorySignals empty() {
            return new HistorySignals(List.of(), null, null, GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS);
        }

        boolean hasMultipleDistinctUniversities() {
            return distinctUniversities != null && distinctUniversities.size() > 1;
        }

        int distinctUniversityCount() {
            return distinctUniversities == null ? 0 : distinctUniversities.size();
        }

        boolean canInheritProgramIntent(boolean currentFollowUp) {
            return currentFollowUp && latestIntent == GraduateKnowledgeIntent.PROGRAM_LOOKUP;
        }

        boolean canInheritTuitionIntent(boolean currentFollowUp) {
            return currentFollowUp && latestIntent == GraduateKnowledgeIntent.TUITION_AGGREGATION;
        }
    }
}
