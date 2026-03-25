package com.uniai.cvbuilder.application.service;

import com.uniai.cvbuilder.application.dto.command.UpdatePersonalInfoCommand;
import com.uniai.cvbuilder.application.dto.response.PersonalInfoResponse;
import com.uniai.cvbuilder.application.port.in.PersonalInfoUseCase;
import com.uniai.cvbuilder.domain.builder.PersonalInfoBuilder;
import com.uniai.cvbuilder.domain.model.PersonalInfo;
import com.uniai.cvbuilder.domain.repository.PersonalInfoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.shared.exception.EmailNotFoundException;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service handling retrieval and updates of user personal
 * information leveraged
 * across generated CVs.
 */
@Service
@RequiredArgsConstructor
public class PersonalInfoApplicationService implements PersonalInfoUseCase {

    private final PersonalInfoRepository personalInfoRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PersonalInfoResponse getPersonalInfo(String email) {
        Long userId = getUserId(email);
        PersonalInfo info = personalInfoRepository.findByUserId(userId).orElse(null);
        return toResponse(info, userId);
    }

    @Override
    @Transactional
    public PersonalInfoResponse updatePersonalInfo(String email, UpdatePersonalInfoCommand command) {
        Long userId = getUserId(email);
        PersonalInfo info = personalInfoRepository.findByUserId(userId)
                .orElseGet(() -> PersonalInfoBuilder.forUser(userId).build());

        if (command.getPhone() != null)
            info.setPhone(command.getPhone());
        if (command.getAddress() != null)
            info.setAddress(command.getAddress());
        if (command.getLinkedin() != null)
            info.setLinkedin(command.getLinkedin());
        if (command.getGithub() != null)
            info.setGithub(command.getGithub());
        if (command.getPortfolio() != null)
            info.setPortfolio(command.getPortfolio());
        if (command.getSummary() != null)
            info.setSummary(command.getSummary());
        if (command.getJobTitle() != null)
            info.setJobTitle(command.getJobTitle());
        if (command.getCompany() != null)
            info.setCompany(command.getCompany());
        if (command.getEducation() != null)
            info.setEducationJson(toJson(command.getEducation()));
        if (command.getSkills() != null)
            info.setSkillsJson(toJson(command.getSkills()));
        if (command.getLanguages() != null)
            info.setLanguagesJson(toJson(command.getLanguages()));
        if (command.getExperience() != null)
            info.setExperienceJson(toJson(command.getExperience()));
        if (command.getProjects() != null)
            info.setProjectsJson(toJson(command.getProjects()));
        if (command.getCertificates() != null)
            info.setCertificatesJson(toJson(command.getCertificates()));

        personalInfoRepository.save(info);
        return toResponse(info, userId);
    }

    private PersonalInfoResponse toResponse(PersonalInfo info, Long userId) {
        if (info == null) {
            return PersonalInfoResponse.builder()
                .userId(userId)
                .hasPersonalInfo(false)
                .education(List.of())
                .skills(List.of())
                .languages(List.of())
                .experience(List.of())
                .projects(List.of())
                .certificates(List.of())
                .build();
        }

        List<PersonalInfoResponse.EducationEntryResponse> education = fromJson(
            info.getEducationJson(),
            new TypeReference<>() {
            },
            List.of()
        );
        List<PersonalInfoResponse.SkillEntryResponse> skills = fromJson(
            info.getSkillsJson(),
            new TypeReference<>() {
            },
            List.of()
        );
        List<PersonalInfoResponse.LanguageEntryResponse> languages = fromJson(
            info.getLanguagesJson(),
            new TypeReference<>() {
            },
            List.of()
        );
        List<PersonalInfoResponse.ExperienceEntryResponse> experience = fromJson(
            info.getExperienceJson(),
            new TypeReference<>() {
            },
            List.of()
        );
        List<PersonalInfoResponse.ProjectEntryResponse> projects = fromJson(
            info.getProjectsJson(),
            new TypeReference<>() {
            },
            List.of()
        );
        List<PersonalInfoResponse.CertificateEntryResponse> certificates = fromJson(
            info.getCertificatesJson(),
            new TypeReference<>() {
            },
            List.of()
        );

        boolean hasPersonalInfo = hasText(info.getPhone())
            || hasText(info.getAddress())
            || hasText(info.getLinkedin())
            || hasText(info.getGithub())
            || hasText(info.getPortfolio())
            || hasText(info.getSummary())
            || hasText(info.getJobTitle())
            || hasText(info.getCompany())
            || !education.isEmpty()
            || !skills.isEmpty()
            || !languages.isEmpty()
            || !experience.isEmpty()
            || !projects.isEmpty()
            || !certificates.isEmpty();

        return PersonalInfoResponse.builder()
                .userId(info.getUserId())
            .hasPersonalInfo(hasPersonalInfo)
                .phone(info.getPhone())
                .address(info.getAddress())
                .linkedin(info.getLinkedin())
                .github(info.getGithub())
                .portfolio(info.getPortfolio())
                .summary(info.getSummary())
                .jobTitle(info.getJobTitle())
                .company(info.getCompany())
                .education(education)
                .skills(skills)
                .languages(languages)
                .experience(experience)
                .projects(projects)
                .certificates(certificates)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize personal info list section", ex);
        }
    }

    private <T> T fromJson(String value, TypeReference<T> typeReference, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (JsonProcessingException ex) {
            return fallback;
        }
    }

    private Long getUserId(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(EmailNotFoundException::new);
        return user.getId();
    }
}
