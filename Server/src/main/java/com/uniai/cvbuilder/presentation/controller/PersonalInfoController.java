package com.uniai.cvbuilder.presentation.controller;

import com.uniai.cvbuilder.application.dto.command.UpdatePersonalInfoCommand;
import com.uniai.cvbuilder.application.dto.response.PersonalInfoResponse;
import com.uniai.cvbuilder.application.port.in.PersonalInfoUseCase;
import com.uniai.shared.infrastructure.jwt.JwtFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieving and updating authenticated users' personal information.
 */
@RestController
@RequestMapping("/api/cv/personal-info")
@RequiredArgsConstructor
public class PersonalInfoController {

    private final JwtFacade jwtFacade;
    private final PersonalInfoUseCase personalInfoUseCase;

    @GetMapping
    public ResponseEntity<PersonalInfoResponse> getPersonalInfo() {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(personalInfoUseCase.getPersonalInfo(email));
    }

    @PutMapping
    public ResponseEntity<PersonalInfoResponse> updatePersonalInfo(@Valid @RequestBody UpdatePersonalInfoCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(personalInfoUseCase.updatePersonalInfo(email, command));
    }

    @GetMapping("/status")
    public ResponseEntity<java.util.Map<String, Object>> getPersonalInfoStatus() {
        String email = jwtFacade.getAuthenticatedUserEmail();
        PersonalInfoResponse info = personalInfoUseCase.getPersonalInfo(email);
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("isFilled", info.isFilled());
        
        java.util.List<String> missingFields = new java.util.ArrayList<>();
        if (info.getPhone() == null || info.getPhone().trim().isEmpty()) missingFields.add("phone");
        if (info.getAddress() == null || info.getAddress().trim().isEmpty()) missingFields.add("address");
        if (info.getSummary() == null || info.getSummary().trim().isEmpty()) missingFields.add("summary");
        if (info.getSkills() == null || info.getSkills().isEmpty()) missingFields.add("skills");
        
        response.put("missingFields", missingFields);
        return ResponseEntity.ok(response);
    }
}
