package com.uniai.admin.presentation.controller;

import com.uniai.admin.application.dto.response.AdminOverviewResponse;
import com.uniai.admin.application.dto.response.AdminUserDetailsResponse;
import com.uniai.admin.application.dto.response.AdminUserFeedbackResponse;
import com.uniai.admin.application.dto.response.AdminUserSearchResponse;
import com.uniai.admin.application.service.AdminApplicationService;
import com.uniai.cvbuilder.application.dto.response.PersonalInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/users/search")
    public ResponseEntity<List<AdminUserSearchResponse>> searchUsers(@RequestParam(required = false) String email) {
        return ResponseEntity.ok(adminApplicationService.searchUsersByEmail(email));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetailsResponse> getUserDetails(@PathVariable Long userId) {
        return ResponseEntity.ok(adminApplicationService.getUserDetails(userId));
    }

    @GetMapping("/users/{userId}/personal-info")
    public ResponseEntity<PersonalInfoResponse> getUserPersonalInfo(@PathVariable Long userId) {
        return ResponseEntity.ok(adminApplicationService.getUserPersonalInfo(userId));
    }

    @GetMapping("/users/{userId}/feedback")
    public ResponseEntity<List<AdminUserFeedbackResponse>> getUserFeedback(@PathVariable Long userId) {
        return ResponseEntity.ok(adminApplicationService.getUserFeedback(userId));
    }

    public record AdminHealthResponse(String message) {}
}
