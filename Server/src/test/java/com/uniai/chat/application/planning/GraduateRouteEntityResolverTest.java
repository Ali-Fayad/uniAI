package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.catalog.domain.model.UniversityCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraduateRouteEntityResolverTest {
    private GraduateRoutePlanParser parser;
    private GraduateRouteEntityResolver resolver;
    private List<UniversityCatalog> catalogs;

    @BeforeEach
    void setUp() {
        parser = new GraduateRoutePlanParser(new GraduateAiRouteCatalog(), new ObjectMapper());
        resolver = new GraduateRouteEntityResolver();
        catalogs = List.of(
                university(1L, "American University of Beirut", "AUB"),
                university(2L, "Lebanese American University", "LAU"),
                university(11L, "Lebanese University", "UL"),
                university(22L, "University of Balamand", "UOB"),
                university(27L, "Lebanese International University", "LIU"));
    }

    @Test
    void resolvesCanonicalNamesAcronymsAndReusableAliases() {
        assertResolved("AUB", 1L);
        assertResolved("LAU", 2L);
        assertResolved("LU", 11L);
        assertResolved("UL", 11L);
        assertResolved("UOB", 22L);
        assertResolved("Balamand", 22L);
        assertResolved("Balamand University", 22L);
        assertResolved("Balamand uni", 22L);
        assertResolved("University of Balamand", 22L);
        assertResolved("LIU", 27L);
    }

    @Test
    void rejectsUnknownExplicitUniversityInsteadOfDroppingScope() {
        ValidatedGraduateRoutePlan<?> plan = parseUniversity("XYZ University");
        assertThrows(GraduateRoutePlanningException.class,
                () -> resolve(plan, "Programs at XYZ University"));
    }

    @Test
    void canonicalizesResolvedIdentityWithoutExposingDatabaseId() {
        ResolvedGraduateRoutePlan<?> result = resolve(parseUniversity("LU"), "Programs at LU");
        assertEquals("Lebanese University", result.canonicalArguments().get("university").textValue());
        assertEquals(11L, result.universities().get(0).id());
        assertEquals(false, result.canonicalArguments().has("universityId"));
    }

    private void assertResolved(String reference, long expectedId) {
        ResolvedGraduateRoutePlan<?> result = resolve(parseUniversity(reference), "Programs at " + reference);
        assertEquals(expectedId, result.universities().get(0).id(), reference);
    }

    private ValidatedGraduateRoutePlan<?> parseUniversity(String university) {
        return parser.parse("{\"route\":\"LIST_PROGRAMS\",\"arguments\":{\"university\":\""
                + university + "\"}}");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ResolvedGraduateRoutePlan<?> resolve(ValidatedGraduateRoutePlan<?> plan, String message) {
        return resolver.resolve((ValidatedGraduateRoutePlan) plan, catalogs, message);
    }

    private UniversityCatalog university(long id, String name, String acronym) {
        return UniversityCatalog.builder().id(id).name(name).acronym(acronym).country("Lebanon").build();
    }
}
