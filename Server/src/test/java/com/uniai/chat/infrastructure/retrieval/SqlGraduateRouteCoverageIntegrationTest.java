package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.planning.GraduateAiRoute;
import com.uniai.chat.application.planning.GraduateAiRouteRegistry;
import com.uniai.chat.application.port.out.GraduateCatalogRouteDao;
import com.uniai.chat.application.port.out.GraduateSupportRouteDao;
import com.uniai.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "ai.provider=placeholder")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SqlGraduateRouteCoverageIntegrationTest extends PostgresIntegrationTest {
    @Autowired private GraduateAiRouteRegistry registry;
    @Autowired private GraduateCatalogRouteDao catalogDao;
    @Autowired private GraduateSupportRouteDao supportDao;

    @Test
    void everyAdvertisedRouteHasExactlyOneRegisteredHandler() {
        assertEquals(GraduateAiRoute.values().length, registry.handlers().size());
        for (GraduateAiRoute route : GraduateAiRoute.values()) assertTrue(registry.contains(route), route.name());
    }

    @Test
    void normalizedCatalogRoutesReadUniversityCampusFacultyAndDepartmentData() {
        GraduateCatalogRouteDao.CatalogCriteria criteria = new GraduateCatalogRouteDao.CatalogCriteria(
                List.of(), null, null, null, null, null, null, 200);
        assertFalse(catalogDao.findUniversities(criteria).isEmpty());
        assertFalse(catalogDao.findCampuses(criteria).isEmpty());
        assertFalse(catalogDao.findFaculties(criteria).isEmpty());
        assertFalse(catalogDao.findDepartments(criteria).isEmpty());
        assertFalse(catalogDao.universityStatistics(criteria).isEmpty());
    }

    @Test
    void everyPopulatedSupportTableHasAnExecutableTypedReadMethod() {
        GraduateSupportRouteDao.SupportCriteria criteria = new GraduateSupportRouteDao.SupportCriteria(
                List.of(), null, null, null, null, List.of(), null, null, null, null, null, 5);
        assertFalse(supportDao.findAdmissionRequirements(criteria).isEmpty());
        assertFalse(supportDao.findRequiredDocuments(criteria).isEmpty());
        assertFalse(supportDao.findDeadlines(criteria).isEmpty());
        assertFalse(supportDao.findScholarships(criteria).isEmpty());
        assertFalse(supportDao.findFinancialAid(criteria).isEmpty());
        assertFalse(supportDao.findPaymentPlans(criteria).isEmpty());
        assertFalse(supportDao.findAccreditations(criteria).isEmpty());
    }

    @Test
    void supportRoutesMatchProgramMajorWhenAnOfficialDegreeNameIsAlsoPresent() {
        GraduateSupportRouteDao.SupportCriteria criteria = new GraduateSupportRouteDao.SupportCriteria(
                List.of(), "Public Health", null, null, null, List.of(), null, null,
                null, null, null, 20);

        assertFalse(supportDao.findAdmissionRequirements(criteria).isEmpty());
    }
}
