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
import java.math.BigDecimal;

public final class GraduateKnowledgeResolutionSupport {

    private static final Pattern WORD_SPLIT = Pattern.compile("[^A-Za-z0-9+]+");
    private static final Pattern CITY_AFTER_IN = Pattern.compile("\\bin\\s+([\\p{L}][\\p{L}\\s'\\-]{1,60}?)(?:[?!.;,]|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FACULTY_NAME = Pattern.compile("(?:faculty|school)\\s+of\\s+([\\p{L}][\\p{L}\\s'\\-]{1,80}?)(?:\\s+(?:at|in)\\s+|[?!.;,]|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEPARTMENT_NAME = Pattern.compile("department\\s+of\\s+([\\p{L}][\\p{L}\\s'\\-]{1,80}?)(?:\\s+(?:at|in)\\s+|[?!.;,]|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PROGRAM_NAME = Pattern.compile("(?:program(?:me)?|degree)\\s+(?:called|named|in)\\s+['\\\"]?([\\p{L}][\\p{L}0-9&'\\-\\s]{1,100}?)(?:['\\\"]|\\s+(?:at|in)\\s+|[?!.;,]|$)", Pattern.CASE_INSENSITIVE);
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

    public static List<ResolvedUniversity> resolveUniversities(String text, List<UniversityCatalog> catalogs) {
        if (text == null || text.isBlank() || catalogs == null || catalogs.isEmpty()) {
            return List.of();
        }

        return resolveUniversityMention(text, catalogs);
    }

    public static List<ResolvedUniversity> resolveUniversityMentions(
            List<String> mentions,
            List<UniversityCatalog> catalogs
    ) {
        Map<Long, ResolvedUniversity> resolved = new LinkedHashMap<>();
        if (mentions == null) return List.of();
        for (String mention : mentions) {
            for (ResolvedUniversity university : resolveUniversityMention(mention, catalogs)) {
                if (university != null && university.id() != null) resolved.putIfAbsent(university.id(), university);
            }
        }
        return new ArrayList<>(resolved.values());
    }

    private static List<ResolvedUniversity> resolveUniversityMention(String text, List<UniversityCatalog> catalogs) {
        if (text == null || text.isBlank() || catalogs == null || catalogs.isEmpty()) return List.of();
        String normalizedText = normalize(text);
        List<UniversityCatalog> exact = catalogs.stream()
                .filter(catalog -> catalog != null && catalog.getId() != null)
                .filter(catalog -> exactUniversityMatch(normalizedText, catalog))
                .toList();
        if (!exact.isEmpty()) return toResolved(exact);

        List<UniversityCatalog> campusMatches = catalogs.stream()
                .filter(catalog -> catalog != null && catalog.getId() != null)
                .filter(catalog -> catalog.getCampuses() != null && catalog.getCampuses().stream()
                        .anyMatch(campus -> campus != null && hasText(campus.getName())
                                && containsPhrase(normalizedText, normalize(campus.getName()))))
                .toList();
        if (campusMatches.size() == 1) return toResolved(campusMatches);
        if (campusMatches.size() > 1) return toResolved(campusMatches);

        List<UniversityCandidate> partial = new ArrayList<>();
        for (UniversityCatalog catalog : catalogs) {
            if (catalog == null || catalog.getId() == null || !hasText(catalog.getName())) continue;
            List<String> distinctiveTokens = tokenize(catalog.getName()).stream()
                    .filter(token -> token.length() > 2 && !GENERIC_UNIVERSITY_TOKENS.contains(token))
                    .distinct()
                    .toList();
            long matched = distinctiveTokens.stream().filter(token -> containsWord(normalizedText, token)).count();
            if (matched >= 2) partial.add(new UniversityCandidate(catalog, (int) matched));
        }
        if (partial.isEmpty()) return List.of();
        int bestScore = partial.stream().mapToInt(UniversityCandidate::score).max().orElse(0);
        return partial.stream().filter(candidate -> candidate.score() == bestScore)
                .map(UniversityCandidate::catalog).toList().stream().map(GraduateKnowledgeResolutionSupport::toResolved).toList();
    }

    private static boolean exactUniversityMatch(String normalizedText, UniversityCatalog university) {
        if (hasText(university.getAcronym()) && containsWord(normalizedText, normalize(university.getAcronym()))) return true;
        if (hasText(university.getName()) && containsPhrase(normalizedText, normalize(university.getName()))) return true;
        return hasText(university.getNameAr()) && containsPhrase(normalizedText, normalize(university.getNameAr()));
    }

    private static boolean containsPhrase(String text, String phrase) {
        if (!hasText(text) || !hasText(phrase)) return false;
        String normalizedText = text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
        String normalizedPhrase = phrase.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
        return (" " + normalizedText + " ").contains(" " + normalizedPhrase + " ");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static ResolvedUniversity toResolved(UniversityCatalog catalog) {
        return new ResolvedUniversity(catalog.getId(), catalog.getName(), catalog.getAcronym());
    }

    private static List<ResolvedUniversity> toResolved(List<UniversityCatalog> catalogs) {
        Map<Long, ResolvedUniversity> resolved = new LinkedHashMap<>();
        for (UniversityCatalog catalog : catalogs) {
            if (catalog != null && catalog.getId() != null) resolved.putIfAbsent(catalog.getId(), toResolved(catalog));
        }
        return new ArrayList<>(resolved.values());
    }

    private record UniversityCandidate(UniversityCatalog catalog, int score) {}

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

    static List<String> detectTopicKeywords(String text) {
        if (text == null || text.isBlank()) return List.of();
        String normalized = normalize(text);
        List<String> topics = new ArrayList<>();
        for (String topic : List.of("computer science", "engineering", "medicine", "business administration", "business", "mba", "law", "education")) {
            if (containsAny(normalized, topic)) topics.add(topic);
        }
        return topics;
    }

    static List<String> detectLanguages(String text) {
        if (text == null || text.isBlank()) return List.of();
        String normalized = normalize(text);
        List<String> languages = new ArrayList<>();
        for (String[] language : List.of(
                new String[]{"english", "en"}, new String[]{"french", "fr"},
                new String[]{"arabic", "ar"})) {
            if (containsAny(normalized, language[0]) || containsWord(normalized, language[1])) languages.add(language[0]);
        }
        return languages;
    }

    static List<String> detectAdmissionRequirementTypes(String text) {
        if (text == null || text.isBlank()) return List.of();
        String normalized = normalize(text);
        List<String> requirements = new ArrayList<>();
        for (String[] requirement : List.of(
                new String[]{"gmat", "GMAT"}, new String[]{"gre", "GRE"},
                new String[]{"portfolio", "PORTFOLIO"}, new String[]{"interview", "INTERVIEW"},
                new String[]{"experience", "EXPERIENCE"}, new String[]{"prerequisite", "PREREQUISITE"})) {
            if (containsWord(normalized, requirement[0])) requirements.add(requirement[1]);
        }
        return requirements;
    }

    static String detectProgramName(String text) {
        if (text == null || text.isBlank()) return null;
        java.util.regex.Matcher matcher = PROGRAM_NAME.matcher(text.trim());
        return matcher.find() && matcher.group(1) != null ? matcher.group(1).trim() : null;
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

    static GraduateKnowledgeComparisonDimension detectComparisonDimension(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "best", "better", "which should i choose", "should i choose")) return null;
        if (containsAny(normalized, "campus count", "campus counts", "more campuses", "how many campuses")) return GraduateKnowledgeComparisonDimension.CAMPUS_COUNT;
        if (containsAny(normalized, "more master's programs", "more masters programs", "program count", "more programs", "how many programs")) return GraduateKnowledgeComparisonDimension.PROGRAM_COUNT;
        if (containsAny(normalized, "more faculties", "faculty count", "how many faculties")) return GraduateKnowledgeComparisonDimension.FACULTY_COUNT;
        if (containsAny(normalized, "more departments", "department count", "how many departments")) return GraduateKnowledgeComparisonDimension.DEPARTMENT_COUNT;
        if (containsAny(normalized, "english programs", "french programs", "arabic programs", "teach in english", "language availability")) return GraduateKnowledgeComparisonDimension.LANGUAGE_AVAILABILITY;
        if (containsAny(normalized, "admission requirements", "admissions requirements", "compare admissions")) return GraduateKnowledgeComparisonDimension.ADMISSION_REQUIREMENTS;
        if (containsAny(normalized, "cheapest", "cheaper", "lower tuition", "average tuition", "tuition average")) return GraduateKnowledgeComparisonDimension.TUITION_AVERAGE;
        if (containsAny(normalized, "minimum tuition", "lowest tuition")) return GraduateKnowledgeComparisonDimension.TUITION_MINIMUM;
        if (containsAny(normalized, "maximum tuition", "highest tuition")) return GraduateKnowledgeComparisonDimension.TUITION_MAXIMUM;
        if (containsAny(normalized, "tuition range", "range of tuition")) return GraduateKnowledgeComparisonDimension.TUITION_RANGE;
        if (containsAny(normalized, "offer it", "offers it", "does it offer")) return GraduateKnowledgeComparisonDimension.PROGRAM_AVAILABILITY;
        if (containsAny(normalized, "compare", "comparison", " vs ", "versus", "between")) return GraduateKnowledgeComparisonDimension.UNIVERSITY;
        return null;
    }

    static boolean isSubjectiveComparison(String text) {
        String normalized = normalize(text);
        return containsAny(normalized, "best", "better", "which should i choose", "should i choose")
                && detectComparisonDimension(normalized) == null;
    }

    static GraduateKnowledgeAggregationFunction detectTuitionAggregation(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "range", "minimum and maximum", "min and max")) return GraduateKnowledgeAggregationFunction.RANGE;
        if (containsAny(normalized, "most expensive", "highest tuition", "maximum tuition", "max tuition")) return GraduateKnowledgeAggregationFunction.MAX;
        if (containsAny(normalized, "cheapest", "lowest tuition", "minimum tuition", "min tuition")) return GraduateKnowledgeAggregationFunction.MIN;
        return GraduateKnowledgeAggregationFunction.AVG;
    }

    static GraduateKnowledgeThresholdOperator detectTuitionThresholdOperator(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "at most", "no more than", "less than or equal")) return GraduateKnowledgeThresholdOperator.LTE;
        if (containsAny(normalized, "at least", "no less than", "greater than or equal")) return GraduateKnowledgeThresholdOperator.GTE;
        if (containsAny(normalized, "under", "below", "less than")) return GraduateKnowledgeThresholdOperator.LT;
        if (containsAny(normalized, "over", "above", "more than", "greater than")) return GraduateKnowledgeThresholdOperator.GT;
        return GraduateKnowledgeThresholdOperator.NONE;
    }

    static BigDecimal detectTuitionThresholdValue(String text) {
        if (text == null || text.isBlank()) return null;
        java.util.regex.Matcher matcher = Pattern.compile("(?:under|below|over|above|at most|at least|less than|more than)\\s+(?:usd|eur|lbp|ll|\\$|€)?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE).matcher(text);
        if (!matcher.find()) return null;
        try { return new BigDecimal(matcher.group(1).replace(",", "")); } catch (NumberFormatException ex) { return null; }
    }

    static String detectTuitionCurrency(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "usd", "dollar", "dollars", "$")) return "USD";
        if (containsAny(normalized, "eur", "euro", "euros", "€")) return "EUR";
        if (containsAny(normalized, "lbp", "l.l.", "lebanese pound", "lebanese pounds")) return "LBP";
        if (containsWord(normalized, "ll")) return "LL";
        return null;
    }

    static String detectTuitionBillingBasis(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "per credit", "credit hour", "credit")) return "PER_CREDIT";
        if (containsAny(normalized, "per semester", "semester")) return "PER_SEMESTER";
        if (containsAny(normalized, "per year", "yearly", "annual", "annually")) return "PER_YEAR";
        if (containsAny(normalized, "per term", "term")) return "PER_TERM";
        if (containsAny(normalized, "per program", "program cost", "total program")) return "PER_PROGRAM";
        return null;
    }

    static String detectTuitionAcademicYear(String text) {
        if (text == null) return null;
        java.util.regex.Matcher matcher = Pattern.compile("\\b(20\\d{2}\\s*[-/]\\s*20\\d{2})\\b").matcher(text);
        return matcher.find() ? matcher.group(1).replaceAll("\\s+", "") : null;
    }

    static String detectTuitionScope(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "by program", "program-level")) return "PROGRAM";
        if (containsAny(normalized, "by department", "department-level")) return "DEPARTMENT";
        if (containsAny(normalized, "by faculty", "faculty-level")) return "FACULTY";
        if (containsAny(normalized, "university-level", "university wide", "university-wide")) return "UNIVERSITY";
        return null;
    }

    static GraduateKnowledgeSort detectTuitionSort(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "cheapest", "lowest tuition", "sort by lowest", "ascending tuition")) {
            return new GraduateKnowledgeSort(GraduateKnowledgeSortField.TUITION, GraduateKnowledgeSortDirection.ASC);
        }
        if (containsAny(normalized, "most expensive", "highest tuition", "sort by highest", "descending tuition")) {
            return new GraduateKnowledgeSort(GraduateKnowledgeSortField.TUITION, GraduateKnowledgeSortDirection.DESC);
        }
        return GraduateKnowledgeSort.empty();
    }

    static Integer detectTuitionLimit(String text) {
        if (text == null) return null;
        java.util.regex.Matcher matcher = Pattern.compile("\\b(?:top|bottom|first|show)\\s+(\\d{1,2})\\b").matcher(normalize(text));
        if (!matcher.find()) return null;
        int value = Integer.parseInt(matcher.group(1));
        return value >= 1 && value <= GraduateKnowledgeQuery.MAX_LIMIT ? value : null;
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
                "how many universities", "number of universities", "universities do we have",
                "which universities are in", "which university is in",
                "universities in", "university in", "universities located in",
                "university located in");
    }

    static boolean detectAcademicStructureLookupIntent(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = normalize(text);
        boolean facultyOrDepartment = containsAny(normalized, "faculty", "faculties", "school of", "department", "departments");
        boolean boundedProgramCheck = containsAny(normalized, "how many", "number of", "is there", "are there", "exist")
                && containsAny(normalized, "program", "programs", "degree", "degrees");
        return facultyOrDepartment || boundedProgramCheck;
    }

    static GraduateKnowledgeResource detectAcademicResource(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "department", "departments")) {
            return GraduateKnowledgeResource.DEPARTMENT;
        }
        if (containsAny(normalized, "faculty", "faculties", "school of")) {
            return GraduateKnowledgeResource.FACULTY;
        }
        return GraduateKnowledgeResource.PROGRAM;
    }

    static GraduateKnowledgeOperation detectAcademicOperation(String text) {
        String normalized = normalize(text);
        if (containsAny(normalized, "what faculties", "which faculties", "what departments", "which departments")) {
            return GraduateKnowledgeOperation.LIST;
        }
        if (containsAny(normalized, "how many", "number of", "count")) {
            return GraduateKnowledgeOperation.COUNT;
        }
        if (containsAny(normalized, "does ", "do ", "is there", "are there", "exist", "offer", "offers")) {
            return GraduateKnowledgeOperation.EXISTS;
        }
        return GraduateKnowledgeOperation.LIST;
    }

    static String detectFacultyName(String text) {
        return extractNamedStructure(text, FACULTY_NAME);
    }

    static String detectDepartmentName(String text) {
        return extractNamedStructure(text, DEPARTMENT_NAME);
    }

    private static String extractNamedStructure(String text, Pattern pattern) {
        if (text == null || text.isBlank()) {
            return null;
        }
        java.util.regex.Matcher matcher = pattern.matcher(text.trim());
        return matcher.find() && matcher.group(1) != null ? matcher.group(1).trim() : null;
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
        String campusMentionCity = catalogs.stream()
                .flatMap(university -> university.getCampuses() == null ? java.util.stream.Stream.empty()
                        : university.getCampuses().stream())
                .filter(campus -> campus != null && campus.getName() != null && campus.getCity() != null
                        && normalized.contains(normalize(campus.getName())))
                .map(com.uniai.catalog.domain.model.CampusCatalog::getCity)
                .findFirst()
                .orElse(null);
        if (campusMentionCity != null) return campusMentionCity;
        String campusCity = catalogs.stream()
                .flatMap(university -> university.getCampuses() == null ? java.util.stream.Stream.empty()
                        : university.getCampuses().stream().map(com.uniai.catalog.domain.model.CampusCatalog::getCity))
                .filter(city -> city != null && !city.isBlank())
                .distinct()
                .sorted((left, right) -> Integer.compare(right.length(), left.length()))
                .filter(city -> normalized.contains(normalize(city)))
                .findFirst()
                .orElse(null);
        if (campusCity != null) return campusCity;
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
        if (detectAcademicStructureLookupIntent(normalizedMessage)) {
            return GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP;
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
