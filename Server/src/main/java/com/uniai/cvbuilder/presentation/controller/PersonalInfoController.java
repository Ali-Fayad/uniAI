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
}
