package com.uniai.admin.catalog.service;

import com.uniai.admin.catalog.dto.CreatePositionRequest;
import com.uniai.admin.catalog.dto.CreateSkillRequest;
import com.uniai.catalog.domain.model.PositionCatalog;
import com.uniai.catalog.domain.model.SkillCatalog;
import com.uniai.catalog.domain.repository.PositionCatalogRepository;
import com.uniai.catalog.domain.repository.SkillCatalogRepository;
import com.uniai.shared.exception.AlreadyExistsException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminCatalogServiceTest {

    @Test
    void normalizesSkillNamesAndRejectsNormalizedDuplicates() {
        FakeSkills skills = new FakeSkills();
        AdminCatalogService service = new AdminCatalogService(skills, new FakePositions());

        service.createSkill(new CreateSkillRequest("  Software   Engineer ", null));

        assertEquals("Software Engineer", skills.rows.get(0).getName());
        assertThrows(AlreadyExistsException.class,
                () -> service.createSkill(new CreateSkillRequest("software engineer", null)));
    }

    @Test
    void createsPositionsWithTheExistingSchema() {
        FakePositions positions = new FakePositions();
        AdminCatalogService service = new AdminCatalogService(new FakeSkills(), positions);

        service.createPosition(new CreatePositionRequest("Data Analyst"));

        assertEquals("Data Analyst", positions.rows.get(0).getName());
    }

    private static final class FakeSkills implements SkillCatalogRepository {
        private final List<SkillCatalog> rows = new ArrayList<>();
        public List<SkillCatalog> findAll() { return rows; }
        public List<SkillCatalog> searchByName(String search) { return rows; }
        public List<SkillCatalog> searchByNameOrCategory(String search) { return rows; }
        public Optional<SkillCatalog> findByNameIgnoreCase(String name) { return Optional.empty(); }
        public SkillCatalog save(SkillCatalog skill) { rows.add(skill); return skill; }
    }

    private static final class FakePositions implements PositionCatalogRepository {
        private final List<PositionCatalog> rows = new ArrayList<>();
        public List<PositionCatalog> findAll() { return rows; }
        public List<PositionCatalog> searchByName(String search) { return rows; }
        public Optional<PositionCatalog> findByNameIgnoreCase(String name) { return Optional.empty(); }
        public PositionCatalog save(PositionCatalog position) { rows.add(position); return position; }
    }
}
