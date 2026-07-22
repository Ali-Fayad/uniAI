package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.catalog.domain.model.CampusCatalog;
import com.uniai.catalog.domain.model.UniversityCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateRouteDeterministicFallbackTest {
    @Test
    void recoversAnUnusableCampusPlanOnlyWhenUniversityAndCityAreUnambiguous() {
        GraduateRouteDeterministicFallback fallback = new GraduateRouteDeterministicFallback(
                new GraduateRoutePlanParser(new GraduateAiRouteCatalog(), new ObjectMapper()));
        UniversityCatalog lau = UniversityCatalog.builder()
                .id(2L).name("Lebanese American University").acronym("LAU")
                .campuses(List.of(CampusCatalog.builder().name("Beirut Campus").city("Beirut").build()))
                .build();

        ValidatedGraduateRoutePlan<?> plan = fallback.plan(
                "what campuses does LAU have in Beirut?", List.of(lau)).orElseThrow();

        assertEquals(GraduateAiRoute.LIST_CAMPUSES, plan.route());
        GraduateRouteArguments.ListCampusesArguments args =
                assertInstanceOf(GraduateRouteArguments.ListCampusesArguments.class, plan.arguments());
        assertEquals("Lebanese American University", args.university());
        assertEquals("Beirut", args.city());
    }

    @Test
    void doesNotBroadenAnUnresolvableRequest() {
        GraduateRouteDeterministicFallback fallback = new GraduateRouteDeterministicFallback(
                new GraduateRoutePlanParser(new GraduateAiRouteCatalog(), new ObjectMapper()));

        assertTrue(fallback.plan("what campuses does XYZ have?", List.of()).isEmpty());
    }
}
