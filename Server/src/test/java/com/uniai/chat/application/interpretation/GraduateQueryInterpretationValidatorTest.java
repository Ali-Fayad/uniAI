package com.uniai.chat.application.interpretation;

import com.uniai.catalog.domain.model.UniversityCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateQueryInterpretationValidatorTest {

    private GraduateQueryInterpretationValidator validator;
    private List<UniversityCatalog> catalogs;

    @BeforeEach
    void setUp() {
        validator = new GraduateQueryInterpretationValidator();
        catalogs = List.of(
                university(1L, "American University of Beirut", "AUB", "الجامعة الأميركية في بيروت"),
                university(2L, "Université Saint-Joseph", "USJ", "الجامعة القديس يوسف")
        );
    }

    @Test
    void shouldValidateAndNormalizeProgramLookup() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                " program_lookup ",
                List.of("AUB"),
                List.of(" master "),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals(1, result.resolvedUniversityCount());
        assertEquals(1, result.degreeTypeCount());
        assertEquals("AUB", result.query().resolvedUniversities().get(0).acronym());
    }

    @Test
    void shouldResolveArabicUniversityNames() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("الجامعة الأميركية في بيروت"),
                List.of("MASTER"),
                "DETAILS",
                null,
                null,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals("AUB", result.query().resolvedUniversities().get(0).acronym());
    }

    @Test
    void shouldMarkUnsupportedForBachelorRequests() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("AUB"),
                List.of("BACHELOR"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.UNSUPPORTED, result.status());
        assertNull(result.query());
        assertTrue(result.unsupportedConstraints().contains("BACHELOR"));
    }

    @Test
    void shouldMarkAmbiguousForInventedUniversityNames() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("Imaginary University"),
                List.of("MASTER"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.AMBIGUOUS, result.status());
        assertNotNull(result.query());
        assertTrue(result.query().resolvedUniversities().isEmpty());
        assertEquals(List.of("MASTER"), result.query().degreeTypes());
    }

    @Test
    void shouldDeduplicateUniversityMentions() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("AUB", "American University of Beirut"),
                List.of("MASTER"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals(1, result.query().resolvedUniversities().size());
    }

    private UniversityCatalog university(Long id, String name, String acronym, String nameAr) {
        return UniversityCatalog.builder()
                .id(id)
                .name(name)
                .acronym(acronym)
                .nameAr(nameAr)
                .build();
    }
}
