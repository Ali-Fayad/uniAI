package com.uniai.catalog.presentation.controller;

import com.uniai.catalog.application.dto.response.LanguageCatalogResponse;
import com.uniai.catalog.application.dto.response.PositionCatalogResponse;
import com.uniai.catalog.application.dto.response.SkillCatalogResponse;
import com.uniai.catalog.application.dto.response.UniversityCatalogResponse;
import com.uniai.catalog.application.service.CatalogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogQueryService catalogQueryService;

    @GetMapping("/skills")
    public ResponseEntity<List<SkillCatalogResponse>> getSkills(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(catalogQueryService.getSkills(search));
    }

    @GetMapping("/languages")
    public ResponseEntity<List<LanguageCatalogResponse>> getLanguages(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(catalogQueryService.getLanguages(search));
    }

    @GetMapping("/positions")
    public ResponseEntity<List<PositionCatalogResponse>> getPositions(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(catalogQueryService.getPositions(search));
    }

    @GetMapping("/universities")
    public ResponseEntity<List<UniversityCatalogResponse>> getUniversities(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(catalogQueryService.getUniversities(search));
    }
}
