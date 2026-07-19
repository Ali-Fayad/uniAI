package com.uniai.chat.application.retrieval;

import com.uniai.catalog.domain.model.CampusCatalog;
import com.uniai.catalog.domain.model.UniversityCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateKnowledgeResolutionSupportTest {

    @Test
    void exactAcronymAndCanonicalNameResolveOnlyTheirUniversity() {
        List<UniversityCatalog> catalogs = List.of(
                university(1L, "American University of Beirut", "AUB"),
                university(2L, "Beirut Institute of Science", "BIS")
        );

        assertEquals(List.of("AUB"), acronyms(GraduateKnowledgeResolutionSupport.resolveUniversities("Tell me about AUB", catalogs)));
        assertEquals(List.of("AUB"), acronyms(GraduateKnowledgeResolutionSupport.resolveUniversities("American University of Beirut programs", catalogs)));
    }

    @Test
    void genericLocationAndSubjectTokensDoNotResolveUniversities() {
        List<UniversityCatalog> catalogs = List.of(
                university(1L, "American University of Beirut", "AUB"),
                university(2L, "Beirut Institute of Science", "BIS")
        );

        assertTrue(GraduateKnowledgeResolutionSupport.resolveUniversities("universities in Beirut", catalogs).isEmpty());
        assertTrue(GraduateKnowledgeResolutionSupport.resolveUniversities("science programs", catalogs).isEmpty());
    }

    @Test
    void uniqueCampusNameResolvesOwningUniversityButSharedCampusNameRemainsAmbiguous() {
        UniversityCatalog aub = UniversityCatalog.builder().id(1L).name("American University of Beirut").acronym("AUB").country("Lebanon")
                .campuses(List.of(CampusCatalog.builder().name("Marine Research").city("Batroun").build())).build();
        UniversityCatalog lau = UniversityCatalog.builder().id(2L).name("Lebanese American University").acronym("LAU").country("Lebanon")
                .campuses(List.of(CampusCatalog.builder().name("Main Campus").city("Byblos").build())).build();
        UniversityCatalog usj = UniversityCatalog.builder().id(3L).name("Université Saint-Joseph").acronym("USJ").country("Lebanon")
                .campuses(List.of(CampusCatalog.builder().name("Main Campus").city("Beirut").build())).build();

        assertEquals(List.of("AUB"), acronyms(GraduateKnowledgeResolutionSupport.resolveUniversities("Where is the Marine Research campus?", List.of(aub, lau, usj))));
        assertEquals(List.of("LAU", "USJ"), acronyms(GraduateKnowledgeResolutionSupport.resolveUniversities("Main Campus", List.of(aub, lau, usj))));
    }

    @Test
    void resolvesLuAliasToLebaneseUniversityWithoutMatchingLiu() {
        UniversityCatalog lu = university(11L, "Lebanese University", "UL");
        UniversityCatalog liu = university(27L, "Lebanese International University", "LIU");
        List<UniversityCatalog> catalogs = List.of(lu, liu);

        assertEquals(List.of("UL"), acronyms(GraduateKnowledgeResolutionSupport.resolveUniversities("LU", catalogs)));
        assertEquals(List.of("UL"), acronyms(GraduateKnowledgeResolutionSupport.resolveUniversities("ul", catalogs)));
        assertEquals(List.of("UL"), acronyms(GraduateKnowledgeResolutionSupport.resolveUniversities("Lebanese University", catalogs)));
        assertEquals(List.of("LIU"), acronyms(GraduateKnowledgeResolutionSupport.resolveUniversities("LIU", catalogs)));
        assertEquals(List.of("LIU"), acronyms(GraduateKnowledgeResolutionSupport.resolveUniversities("Lebanese International University", catalogs)));
        assertTrue(GraduateKnowledgeResolutionSupport.resolveUniversities("LU", List.of(liu)).isEmpty());
    }

    @Test
    void distinguishesExplicitUnknownUniversityFromBroadCityQuestion() {
        List<UniversityCatalog> catalogs = List.of(
                university(11L, "Lebanese University", "UL"),
                university(27L, "Lebanese International University", "LIU")
        );

        assertTrue(GraduateKnowledgeResolutionSupport.hasExplicitUniversityReference(
                "Does XYZ University have a campus in Nabatieh?", catalogs));
        assertTrue(!GraduateKnowledgeResolutionSupport.hasExplicitUniversityReference(
                "Which universities have campuses in Nabatieh?", catalogs));
    }

    private UniversityCatalog university(Long id, String name, String acronym) {
        return UniversityCatalog.builder().id(id).name(name).acronym(acronym).country("Lebanon").build();
    }

    private List<String> acronyms(List<ResolvedUniversity> universities) {
        return universities.stream().map(ResolvedUniversity::acronym).toList();
    }
}
