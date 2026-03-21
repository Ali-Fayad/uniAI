package com.uniai.cvbuilder.presentation.controller;

import com.uniai.cvbuilder.application.dto.command.*;
import com.uniai.cvbuilder.application.dto.response.*;
import com.uniai.cvbuilder.application.port.in.CVUseCase;
import com.uniai.cvbuilder.application.service.ExternalApiApplicationService;
import com.uniai.shared.infrastructure.jwt.JwtFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CVController {

    private final JwtFacade jwtFacade;
    private final CVUseCase cvUseCase;
    private final ExternalApiApplicationService externalApiApplicationService;

    @GetMapping
    public ResponseEntity<List<CVResponse>> getCvs() {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(cvUseCase.getUserCVs(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CVResponse> getCv(@PathVariable Long id) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(cvUseCase.getCV(email, id));
    }

    @PostMapping
    public ResponseEntity<CVResponse> createCv(@Valid @RequestBody CreateCVCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        CVResponse response = cvUseCase.createCV(email, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CVResponse> updateCv(@PathVariable Long id, @Valid @RequestBody UpdateCVCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(cvUseCase.updateCV(email, id, command));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCv(@PathVariable Long id) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        cvUseCase.deleteCV(email, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cvId}/education")
    public ResponseEntity<EducationResponse> addEducation(@PathVariable Long cvId, @Valid @RequestBody AddEducationCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(cvUseCase.addEducation(email, cvId, command));
    }

    @PutMapping("/education/{id}")
    public ResponseEntity<EducationResponse> updateEducation(@PathVariable Long id, @Valid @RequestBody AddEducationCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(cvUseCase.updateEducation(email, id, command));
    }

    @DeleteMapping("/education/{id}")
    public ResponseEntity<Void> deleteEducation(@PathVariable Long id) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        cvUseCase.deleteEducation(email, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cvId}/experience")
    public ResponseEntity<ExperienceResponse> addExperience(@PathVariable Long cvId, @Valid @RequestBody AddExperienceCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(cvUseCase.addExperience(email, cvId, command));
    }

    @PutMapping("/experience/{id}")
    public ResponseEntity<ExperienceResponse> updateExperience(@PathVariable Long id, @Valid @RequestBody AddExperienceCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(cvUseCase.updateExperience(email, id, command));
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        cvUseCase.deleteExperience(email, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cvId}/skill")
    public ResponseEntity<SkillResponse> addSkill(@PathVariable Long cvId, @Valid @RequestBody AddSkillCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(cvUseCase.addSkill(email, cvId, command));
    }

    @PutMapping("/skill/{id}")
    public ResponseEntity<SkillResponse> updateSkill(@PathVariable Long id, @Valid @RequestBody AddSkillCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(cvUseCase.updateSkill(email, id, command));
    }

    @DeleteMapping("/skill/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        cvUseCase.deleteSkill(email, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cvId}/project")
    public ResponseEntity<ProjectResponse> addProject(@PathVariable Long cvId, @Valid @RequestBody AddProjectCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(cvUseCase.addProject(email, cvId, command));
    }

    @PutMapping("/project/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @Valid @RequestBody AddProjectCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(cvUseCase.updateProject(email, id, command));
    }

    @DeleteMapping("/project/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        cvUseCase.deleteProject(email, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cvId}/language")
    public ResponseEntity<LanguageResponse> addLanguage(@PathVariable Long cvId, @Valid @RequestBody AddLanguageCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(cvUseCase.addLanguage(email, cvId, command));
    }

    @PutMapping("/language/{id}")
    public ResponseEntity<LanguageResponse> updateLanguage(@PathVariable Long id, @Valid @RequestBody AddLanguageCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(cvUseCase.updateLanguage(email, id, command));
    }

    @DeleteMapping("/language/{id}")
    public ResponseEntity<Void> deleteLanguage(@PathVariable Long id) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        cvUseCase.deleteLanguage(email, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cvId}/certificate")
    public ResponseEntity<CertificateResponse> addCertificate(@PathVariable Long cvId, @Valid @RequestBody AddCertificateCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(cvUseCase.addCertificate(email, cvId, command));
    }

    @PutMapping("/certificate/{id}")
    public ResponseEntity<CertificateResponse> updateCertificate(@PathVariable Long id, @Valid @RequestBody AddCertificateCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(cvUseCase.updateCertificate(email, id, command));
    }

    @DeleteMapping("/certificate/{id}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long id) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        cvUseCase.deleteCertificate(email, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/skills")
    public ResponseEntity<List<String>> getSkills() {
        return ResponseEntity.ok(externalApiApplicationService.getSkills());
    }

    @GetMapping("/positions")
    public ResponseEntity<List<String>> getPositions() {
        return ResponseEntity.ok(externalApiApplicationService.getPositions());
    }

    @GetMapping("/universities")
    public ResponseEntity<List<UniversityResponse>> getUniversities() {
        return ResponseEntity.ok(cvUseCase.getUniversities());
    }
}
