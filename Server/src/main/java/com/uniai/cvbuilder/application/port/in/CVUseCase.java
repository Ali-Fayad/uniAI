package com.uniai.cvbuilder.application.port.in;

import com.uniai.cvbuilder.application.dto.command.*;
import com.uniai.cvbuilder.application.dto.response.*;

import java.util.List;

/**
 * Application boundary for all CV management operations, including CRUD on the CV shell and
 * its nested sections.
 */
public interface CVUseCase {

    List<CVTemplateResponse> getTemplates();

    CVTemplateResponse getTemplate(Long templateId);

    List<CVResponse> getUserCVs(String email);

    CVResponse getCV(String email, Long cvId);

    CVResponse createCV(String email, CreateCVCommand command);

    CVResponse updateCV(String email, Long cvId, UpdateCVCommand command);

    void deleteCV(String email, Long cvId);

    EducationResponse addEducation(String email, Long cvId, AddEducationCommand command);

    EducationResponse updateEducation(String email, Long educationId, AddEducationCommand command);

    void deleteEducation(String email, Long educationId);

    ExperienceResponse addExperience(String email, Long cvId, AddExperienceCommand command);

    ExperienceResponse updateExperience(String email, Long experienceId, AddExperienceCommand command);

    void deleteExperience(String email, Long experienceId);

    SkillResponse addSkill(String email, Long cvId, AddSkillCommand command);

    SkillResponse updateSkill(String email, Long skillId, AddSkillCommand command);

    void deleteSkill(String email, Long skillId);

    ProjectResponse addProject(String email, Long cvId, AddProjectCommand command);

    ProjectResponse updateProject(String email, Long projectId, AddProjectCommand command);

    void deleteProject(String email, Long projectId);

    LanguageResponse addLanguage(String email, Long cvId, AddLanguageCommand command);

    LanguageResponse updateLanguage(String email, Long languageId, AddLanguageCommand command);

    void deleteLanguage(String email, Long languageId);

    CertificateResponse addCertificate(String email, Long cvId, AddCertificateCommand command);

    CertificateResponse updateCertificate(String email, Long certificateId, AddCertificateCommand command);

    void deleteCertificate(String email, Long certificateId);

    List<UniversityResponse> getUniversities();
}
