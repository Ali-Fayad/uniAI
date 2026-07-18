package com.uniai.catalog.application.service;

import com.uniai.catalog.application.dto.response.UniversityCatalogResponse;
import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.model.CampusCatalog;
import com.uniai.catalog.infrastructure.persistence.repository.CampusCatalogJpaRepository;
import com.uniai.catalog.infrastructure.persistence.repository.UniversityCatalogJpaRepository;
import com.uniai.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "ai.provider=placeholder")
@Transactional
class CatalogQueryServiceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private CatalogQueryService catalogQueryService;

    @Autowired
    private UniversityCatalogJpaRepository universityCatalogJpaRepository;

    @Autowired
    private CampusCatalogJpaRepository campusCatalogJpaRepository;

    @Test
    void getUniversitiesShouldReturnOneLogicalUniversityForAcronymAndFullNameSearches() {
        assertOneResult("AUB", "AUB", "American University of Beirut");
        assertOneResult("MU", "MU", "Al Maaref University");
        assertOneResult("BAU", "BAU", "Beirut Arab University");
        assertOneResult("Beirut Arab", "BAU", "Beirut Arab University");
    }

    @Test
    void getUniversitiesShouldMatchArabicNamesWhenDataExists() {
        UniversityCatalog catalog = university(999001L, "Test University of Beirut", "TUB", "جامعة الاختبار", null, null);
        universityCatalogJpaRepository.save(catalog);

        List<UniversityCatalogResponse> result = catalogQueryService.getUniversities("جامعة الاختبار");

        assertEquals(1, result.size());
        assertEquals("TUB", result.get(0).acronym());
        assertEquals("جامعة الاختبار", result.get(0).nameAr());
    }

    @Test
    void getUniversitiesShouldCollapseCampusVariantsIntoOneLogicalInstitution() {
        UniversityCatalog institutionLevel = university(999103L, "Test University", "TST", null, null, null);
        UniversityCatalog saved = universityCatalogJpaRepository.save(institutionLevel);
        campusCatalogJpaRepository.saveAll(List.of(
                CampusCatalog.builder().universityId(saved.getId()).name("Main Campus").city("Beirut").campusType("Main").build(),
                CampusCatalog.builder().universityId(saved.getId()).name("Branch").city("Tripoli").campusType("Branch").build()));

        List<UniversityCatalogResponse> result = catalogQueryService.getUniversities("TST");

        assertEquals(1, result.size());
        assertEquals(999103L, result.get(0).id());
        assertEquals("TST", result.get(0).acronym());
        assertTrue(result.get(0).name().contains("Test University"));
    }

    private void assertOneResult(String search, String expectedAcronym, String expectedName) {
        List<UniversityCatalogResponse> result = catalogQueryService.getUniversities(search);

        assertEquals(1, result.size(), search);
        assertEquals(expectedAcronym, result.get(0).acronym(), search);
        assertEquals(expectedName, result.get(0).name(), search);
    }

    private UniversityCatalog university(Long id, String name, String acronym, String nameAr, String campusName, String campusType) {
        return UniversityCatalog.builder()
                .id(id)
                .name(name)
                .acronym(acronym)
                .country("Lebanon")
                .nameAr(nameAr)
                .campusName(campusName)
                .campusType(campusType)
                .build();
    }
}
