package com.uniai.admin.catalog.presentation;

import com.uniai.admin.catalog.dto.CatalogAdminResponse;
import com.uniai.admin.catalog.dto.CreatePositionRequest;
import com.uniai.admin.catalog.dto.CreateSkillRequest;
import com.uniai.admin.catalog.service.AdminCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/catalog")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final AdminCatalogService service;

    @GetMapping("/skills")
    public ResponseEntity<List<CatalogAdminResponse>> searchSkills(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(service.searchSkills(query, limit));
    }

    @PostMapping("/skills")
    public ResponseEntity<CatalogAdminResponse> createSkill(@Valid @RequestBody CreateSkillRequest request) {
        return ResponseEntity.ok(service.createSkill(request));
    }

    @GetMapping("/positions")
    public ResponseEntity<List<CatalogAdminResponse>> searchPositions(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(service.searchPositions(query, limit));
    }

    @PostMapping("/positions")
    public ResponseEntity<CatalogAdminResponse> createPosition(@Valid @RequestBody CreatePositionRequest request) {
        return ResponseEntity.ok(service.createPosition(request));
    }
}
