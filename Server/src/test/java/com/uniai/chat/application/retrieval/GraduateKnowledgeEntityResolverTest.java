package com.uniai.chat.application.retrieval;

import com.uniai.catalog.domain.model.UniversityCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateKnowledgeEntityResolverTest {
    private final GraduateKnowledgeEntityResolver resolver = new GraduateKnowledgeEntityResolver();
    private final List<UniversityCatalog> catalogs = List.of(
            university(11L, "Lebanese University", "UL"),
            university(22L, "University of Balamand", "UOB"),
            university(27L, "Lebanese International University", "LIU"),
            university(1L, "American University of Beirut", "AUB"),
            university(2L, "Lebanese American University", "LAU")
    );

    @Test
    void resolvesCanonicalNamesAndApprovedAliases() {
        assertEquals(11L, resolver.resolve(List.of("LU"), catalogs, null).universities().get(0).id());
        assertEquals(11L, resolver.resolve(List.of("UL"), catalogs, null).universities().get(0).id());
        assertEquals(22L, resolver.resolve(List.of("Balamand uni"), catalogs, null).universities().get(0).id());
        assertEquals(27L, resolver.resolve(List.of("LIU"), catalogs, null).universities().get(0).id());
        assertEquals(1L, resolver.resolve(List.of("AUB"), catalogs, null).universities().get(0).id());
        assertEquals(2L, resolver.resolve(List.of("LAU"), catalogs, null).universities().get(0).id());
    }

    @Test
    void resolvesUniqueTypoButDoesNotResolveUnknownExplicitUniversity() {
        GraduateKnowledgeEntityResolutionResult typo = resolver.resolve(List.of("Balamnd"), catalogs, null);
        assertEquals(GraduateKnowledgeEntityResolutionStatus.RESOLVED, typo.status());
        assertEquals(22L, typo.universities().get(0).id());

        GraduateKnowledgeEntityResolutionResult unknown = resolver.resolve(
                List.of(), catalogs, "Does XYZ University have a campus in Nabatieh?");
        assertEquals(GraduateKnowledgeEntityResolutionStatus.UNKNOWN, unknown.status());
        assertTrue(unknown.explicitReference());
    }

    @Test
    void preservesBroadCityQueriesWithoutInventingUniversityScope() {
        GraduateKnowledgeEntityResolutionResult result = resolver.resolve(
                List.of(), catalogs, "Which universities have campuses in Nabatieh?");
        assertEquals(GraduateKnowledgeEntityResolutionStatus.NONE_REQUESTED, result.status());
        assertTrue(result.universities().isEmpty());
        assertTrue(!result.explicitReference());
    }

    private UniversityCatalog university(Long id, String name, String acronym) {
        return UniversityCatalog.builder().id(id).name(name).acronym(acronym).build();
    }
}
