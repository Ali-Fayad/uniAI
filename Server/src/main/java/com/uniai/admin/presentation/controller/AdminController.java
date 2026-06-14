package com.uniai.admin.presentation.controller;

import com.uniai.admin.application.dto.response.AdminOverviewResponse;
import com.uniai.admin.application.service.AdminApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal admin entry point.
 * The controller stays thin and delegates response content to the application service.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminApplicationService adminApplicationService;

    @GetMapping("/health")
    public ResponseEntity<AdminHealthResponse> health() {
        return ResponseEntity.ok(new AdminHealthResponse(adminApplicationService.getHealthMessage()));
    }

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewResponse> overview() {
        return ResponseEntity.ok(adminApplicationService.getOverview());
    }

    public record AdminHealthResponse(String message) {}
}
