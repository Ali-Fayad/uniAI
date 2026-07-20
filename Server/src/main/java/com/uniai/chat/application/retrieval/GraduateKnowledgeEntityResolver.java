package com.uniai.chat.application.retrieval;

import com.uniai.catalog.domain.model.UniversityCatalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Server-owned, deterministic university identity resolution. */
public class GraduateKnowledgeEntityResolver {
    private static final int MAX_UNIVERSITIES = 20;
    private static final double DEFAULT_FUZZY_CONFIDENCE_THRESHOLD = 0.86d;
    private static final Pattern EXPLICIT_UNIVERSITY = Pattern.compile(
            "\\b(?!(?:which|what|how many|number of)\\s+)(?:[\\p{L}\\p{N}][\\p{L}\\p{N}&.'-]*\\s+){1,6}(?:university|college|institute)\\b",
            Pattern.CASE_INSENSITIVE);
    private final double fuzzyConfidenceThreshold;

    public GraduateKnowledgeEntityResolver() {
        this(DEFAULT_FUZZY_CONFIDENCE_THRESHOLD);
    }

    public GraduateKnowledgeEntityResolver(double fuzzyConfidenceThreshold) {
        if (fuzzyConfidenceThreshold <= 0d || fuzzyConfidenceThreshold > 1d) {
            throw new IllegalArgumentException("Fuzzy confidence threshold must be between 0 and 1");
        }
        this.fuzzyConfidenceThreshold = fuzzyConfidenceThreshold;
    }

    public GraduateKnowledgeEntityResolutionResult resolve(
            List<String> references,
            List<UniversityCatalog> catalogs,
            String currentUserMessage
    ) {
        List<UniversityCatalog> safeCatalogs = catalogs == null ? List.of() : catalogs;
        List<String> requested = references == null ? List.of() : references.stream()
                .filter(this::hasText)
                .map(String::trim)
                .limit(MAX_UNIVERSITIES)
                .toList();

        Map<Long, ResolvedUniversity> resolved = new LinkedHashMap<>();
        List<String> unresolved = new ArrayList<>();
        boolean ambiguous = false;

        for (String reference : requested) {
            Match match = find(reference, safeCatalogs);
            if (match.candidates().size() == 1) {
                ResolvedUniversity university = toResolved(match.candidates().get(0));
                resolved.putIfAbsent(university.id(), university);
            } else if (match.candidates().size() > 1) {
                ambiguous = true;
                unresolved.add(reference);
            } else {
                unresolved.add(reference);
            }
        }

        boolean explicitReference = !requested.isEmpty();
        Match messageMatch = find(currentUserMessage, safeCatalogs);
        if (requested.isEmpty() && messageMatch.candidates().size() == 1) {
            ResolvedUniversity university = toResolved(messageMatch.candidates().get(0));
            resolved.putIfAbsent(university.id(), university);
            explicitReference = true;
        } else if (requested.isEmpty() && messageMatch.candidates().size() > 1) {
            ambiguous = true;
            explicitReference = true;
        }
        if (hasUnresolvedExplicitUniversity(currentUserMessage, safeCatalogs, messageMatch)) {
            explicitReference = true;
            if (messageMatch.candidates().isEmpty()) unresolved.add(currentUserMessage.trim());
        }

        List<ResolvedUniversity> universities = List.copyOf(resolved.values());
        GraduateKnowledgeEntityResolutionStatus status;
        if (ambiguous) status = GraduateKnowledgeEntityResolutionStatus.AMBIGUOUS;
        else if (!unresolved.isEmpty()) status = GraduateKnowledgeEntityResolutionStatus.UNKNOWN;
        else if (universities.isEmpty() && !explicitReference) status = GraduateKnowledgeEntityResolutionStatus.NONE_REQUESTED;
        else status = GraduateKnowledgeEntityResolutionStatus.RESOLVED;
        return new GraduateKnowledgeEntityResolutionResult(status, universities, unresolved, explicitReference);
    }

    private Match find(String reference, List<UniversityCatalog> catalogs) {
        if (!hasText(reference) || catalogs.isEmpty()) return Match.empty();
        List<UniversityCatalog> exact = catalogs.stream()
                .filter(catalog -> catalog != null && catalog.getId() != null)
                .filter(catalog -> matchesExact(reference, catalog))
                .toList();
        if (!exact.isEmpty()) return new Match(exact);

        List<ScoredCandidate> fuzzy = new ArrayList<>();
        for (UniversityCatalog catalog : catalogs) {
            if (catalog == null || catalog.getId() == null) continue;
            double score = candidateScore(reference, catalog);
            if (score >= fuzzyConfidenceThreshold) fuzzy.add(new ScoredCandidate(catalog, score));
        }
        if (fuzzy.isEmpty()) return Match.empty();
        double best = fuzzy.stream().mapToDouble(ScoredCandidate::score).max().orElse(0d);
        List<UniversityCatalog> bestCandidates = fuzzy.stream()
                .filter(candidate -> Math.abs(candidate.score() - best) < 0.0001d)
                .map(ScoredCandidate::catalog)
                .toList();
        return new Match(bestCandidates);
    }

    private boolean matchesExact(String reference, UniversityCatalog catalog) {
        String text = normalize(reference);
        for (String value : candidateNames(catalog)) {
            String normalized = normalize(value);
            if (normalized.equals(text) || containsPhrase(text, normalized)) return true;
        }
        return false;
    }

    private double candidateScore(String reference, UniversityCatalog catalog) {
        String normalizedReference = normalize(reference);
        return candidateNames(catalog).stream()
                .map(this::normalize)
                .filter(value -> value.length() >= 4 && normalizedReference.length() >= 4)
                .mapToDouble(value -> similarity(normalizedReference, value))
                .max().orElse(0d);
    }

    private List<String> candidateNames(UniversityCatalog catalog) {
        List<String> values = new ArrayList<>();
        if (hasText(catalog.getName())) values.add(catalog.getName());
        if (hasText(catalog.getAcronym())) values.add(catalog.getAcronym());
        if (hasText(catalog.getNameAr())) values.add(catalog.getNameAr());
        for (Map.Entry<String, String> alias : GraduateKnowledgeUniversityAliases.all().entrySet()) {
            if (matchesCanonical(alias.getValue(), catalog)) values.add(alias.getKey());
        }
        return values;
    }

    private boolean matchesCanonical(String canonical, UniversityCatalog catalog) {
        String normalized = normalize(canonical);
        return normalize(catalog.getName()).equals(normalized) || normalize(catalog.getAcronym()).equals(normalized);
    }

    private boolean hasUnresolvedExplicitUniversity(String message, List<UniversityCatalog> catalogs, Match match) {
        if (!hasText(message) || !match.candidates().isEmpty()) return false;
        String normalized = normalize(message);
        if (isBroadUniversityQuestion(normalized)) return false;
        return EXPLICIT_UNIVERSITY.matcher(message).find() || containsUnresolvedAlias(normalized, catalogs);
    }

    private boolean containsUnresolvedAlias(String normalized, List<UniversityCatalog> catalogs) {
        if (GraduateKnowledgeUniversityAliases.all().keySet().stream().anyMatch(alias -> containsWord(normalized, alias))) {
            return true;
        }
        return catalogs.stream()
                .filter(catalog -> catalog != null && hasText(catalog.getAcronym()))
                .map(UniversityCatalog::getAcronym)
                .anyMatch(acronym -> containsWord(normalized, acronym));
    }

    private boolean isBroadUniversityQuestion(String normalized) {
        return Set.of("which universities", "what universities", "how many universities", "universities in")
                .stream().anyMatch(normalized::contains);
    }

    private double similarity(String left, String right) {
        int distance = levenshtein(left, right);
        return 1d - ((double) distance / Math.max(left.length(), right.length()));
    }

    private int levenshtein(String left, String right) {
        int[] previous = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) previous[j] = j;
        for (int i = 1; i <= left.length(); i++) {
            int[] current = new int[right.length() + 1];
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + (left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1));
            }
            previous = current;
        }
        return previous[right.length()];
    }

    private boolean containsPhrase(String text, String phrase) {
        return (" " + text + " ").contains(" " + phrase + " ");
    }

    private boolean containsWord(String text, String word) {
        return (" " + text + " ").contains(" " + normalize(word) + " ");
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ResolvedUniversity toResolved(UniversityCatalog catalog) {
        return new ResolvedUniversity(catalog.getId(), catalog.getName(), catalog.getAcronym());
    }

    private record Match(List<UniversityCatalog> candidates) {
        private static Match empty() { return new Match(List.of()); }
    }

    private record ScoredCandidate(UniversityCatalog catalog, double score) {}
}
