package com.uniai.cvbuilder.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uniai.cvbuilder.application.dto.command.AddCertificateCommand;
import com.uniai.cvbuilder.application.dto.command.AddEducationCommand;
import com.uniai.cvbuilder.application.dto.command.AddExperienceCommand;
import com.uniai.cvbuilder.application.dto.command.AddLanguageCommand;
import com.uniai.cvbuilder.application.dto.command.AddProjectCommand;
import com.uniai.cvbuilder.application.dto.command.AddSkillCommand;
import com.uniai.cvbuilder.application.dto.command.CreateCVCommand;
import com.uniai.cvbuilder.application.dto.command.UpdateCVCommand;
import com.uniai.cvbuilder.application.dto.response.CVResponse;
import com.uniai.cvbuilder.application.dto.response.CVTemplateResponse;
import com.uniai.cvbuilder.application.dto.response.CertificateResponse;
import com.uniai.cvbuilder.application.dto.response.EducationResponse;
import com.uniai.cvbuilder.application.dto.response.ExperienceResponse;
import com.uniai.cvbuilder.application.dto.response.LanguageResponse;
import com.uniai.cvbuilder.application.dto.response.ProjectResponse;
import com.uniai.cvbuilder.application.dto.response.SkillResponse;
import com.uniai.cvbuilder.application.dto.response.UniversityResponse;
import com.uniai.cvbuilder.application.mapper.CVMapper;
import com.uniai.cvbuilder.application.mapper.CVSelectionMapper;
import com.uniai.cvbuilder.application.mapper.UniversityMapper;
import com.uniai.cvbuilder.application.port.in.CVUseCase;
import com.uniai.cvbuilder.domain.builder.CVBuilder;
import com.uniai.cvbuilder.domain.builder.CertificateBuilder;
import com.uniai.cvbuilder.domain.builder.EducationBuilder;
import com.uniai.cvbuilder.domain.builder.ExperienceBuilder;
import com.uniai.cvbuilder.domain.builder.LanguageBuilder;
import com.uniai.cvbuilder.domain.builder.ProjectBuilder;
import com.uniai.cvbuilder.domain.builder.SkillBuilder;
import com.uniai.cvbuilder.domain.model.CV;
import com.uniai.cvbuilder.domain.model.CVTemplate;
import com.uniai.cvbuilder.domain.model.Certificate;
import com.uniai.cvbuilder.domain.model.Education;
import com.uniai.cvbuilder.domain.model.Experience;
import com.uniai.cvbuilder.domain.model.ItemsOrder;
import com.uniai.cvbuilder.domain.model.Language;
import com.uniai.cvbuilder.domain.model.PersonalInfo;
import com.uniai.cvbuilder.domain.model.Project;
import com.uniai.cvbuilder.domain.model.SelectedItems;
import com.uniai.cvbuilder.domain.model.Skill;
import com.uniai.cvbuilder.domain.repository.CVRepository;
import com.uniai.cvbuilder.domain.repository.CVTemplateRepository;
import com.uniai.cvbuilder.domain.repository.CertificateRepository;
import com.uniai.cvbuilder.domain.repository.EducationRepository;
import com.uniai.cvbuilder.domain.repository.ExperienceRepository;
import com.uniai.cvbuilder.domain.repository.LanguageRepository;
import com.uniai.cvbuilder.domain.repository.PersonalInfoRepository;
import com.uniai.cvbuilder.domain.repository.ProjectRepository;
import com.uniai.cvbuilder.domain.repository.SkillRepository;
import com.uniai.cvbuilder.domain.repository.UniversityRepository;
import com.uniai.shared.exception.CVNotFoundException;
import com.uniai.shared.exception.CVTemplateNotFoundException;
import com.uniai.shared.exception.EmailNotFoundException;
import com.uniai.shared.exception.SectionNotFoundException;
import com.uniai.shared.exception.UnauthorizedAccessException;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Application service coordinating CV lifecycle operations, section CRUD, ownership checks,
 * and mapping domain aggregates to response DTOs.
 */
@Service
@RequiredArgsConstructor
public class CVApplicationService implements CVUseCase {

    private final CVRepository cvRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final LanguageRepository languageRepository;
    private final CertificateRepository certificateRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final CVTemplateRepository cvTemplateRepository;
    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;
    private final CVTemplateResolver templateResolver;
    private final CVSectionOrderPolicy sectionOrderPolicy;

    @Override
    public List<CVTemplateResponse> getTemplates() {
        return cvTemplateRepository.findAllActive().stream()
            .map(template -> CVTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .thumbnailUrl(template.getThumbnailUrl())
                .componentName(template.getComponentName())
                .isActive(template.isActive())
                .build())
            .toList();
    }

    @Override
    public CVTemplateResponse getTemplate(Long templateId) {
        CVTemplate template = cvTemplateRepository.findById(templateId)
            .orElseThrow(CVTemplateNotFoundException::new);
        return CVTemplateResponse.builder()
            .id(template.getId())
            .name(template.getName())
            .description(template.getDescription())
            .thumbnailUrl(template.getThumbnailUrl())
            .componentName(template.getComponentName())
            .isActive(template.isActive())
            .build();
    }

    @Override
    public List<CVResponse> getUserCVs(String email) {
        Long userId = getUserId(email);
        PersonalInfo personalInfo = personalInfoRepository.findByUserId(userId).orElse(null);
        return cvRepository.findByUserId(userId).stream()
                .map(cv -> mapCv(cv, personalInfo))
                .toList();
    }

    @Override
    public CVResponse getCV(String email, Long cvId) {
        Long userId = getUserId(email);
        CV cv = getCvForOwner(cvId, userId);
        PersonalInfo personalInfo = personalInfoRepository.findByUserId(userId).orElse(null);
        return mapCv(cv, personalInfo);
    }

    @Override
    @Transactional
    public CVResponse createCV(String email, CreateCVCommand command) {
        Long userId = getUserId(email);
        boolean makeDefault = Boolean.TRUE.equals(command.getIsDefault());

        if (makeDefault) {
            unsetExistingDefault(userId, null);
        }

        CVTemplate selectedTemplate = templateResolver.resolveForCreate(command);

        CV cv = CVBuilder.newCv(userId, command.getCvName())
            .templateId(selectedTemplate != null ? selectedTemplate.getId() : null)
            .template(selectedTemplate != null ? selectedTemplate.getComponentName() : command.getTemplate())
            .sectionsOrder(sectionOrderPolicy.normalize(command.getSectionsOrder()))
            .isDefault(makeDefault)
            .build();
            
        if (command.getSelectedItems() != null) {
            cv.setSelectedItems(CVSelectionMapper.toSelectedItems(command.getSelectedItems()));
        } else {
            cv.setSelectedItems(new SelectedItems());
        }
        
        if (command.getItemsOrder() != null) {
            cv.setItemsOrder(CVSelectionMapper.toItemsOrder(command.getItemsOrder()));
        } else {
            cv.setItemsOrder(new ItemsOrder());
        }
        
        cvRepository.save(cv);

        PersonalInfo personalInfo = personalInfoRepository.findByUserId(userId).orElse(null);
        return mapCv(cv, personalInfo);
    }

    @Override
    @Transactional
    public CVResponse updateCV(String email, Long cvId, UpdateCVCommand command) {
        Long userId = getUserId(email);
        CV cv = getCvForOwner(cvId, userId);

        if (command.getCvName() != null && !command.getCvName().isBlank()) {
            cv.setCvName(command.getCvName());
        }
        if (command.getTemplate() != null) {
            CVTemplate template = templateResolver.resolve(command.getTemplateId(), command.getTemplate());
            cv.setTemplateId(template != null ? template.getId() : null);
            cv.setTemplate(template != null ? template.getComponentName() : command.getTemplate());
        } else if (command.getTemplateId() != null) {
            CVTemplate template = templateResolver.resolve(command.getTemplateId(), null);
            cv.setTemplateId(template != null ? template.getId() : null);
            cv.setTemplate(template != null ? template.getComponentName() : null);
        }
        if (command.getSectionsOrder() != null && !command.getSectionsOrder().isEmpty()) {
            cv.setSectionsOrder(sectionOrderPolicy.normalize(command.getSectionsOrder()));
        }
        if (command.getIsDefault() != null) {
            if (command.getIsDefault()) {
                unsetExistingDefault(userId, cvId);
                cv.setDefault(true);
            } else {
                cv.setDefault(false);
            }
        }
        
        if (command.getSelectedItems() != null) {
            cv.setSelectedItems(CVSelectionMapper.toSelectedItems(command.getSelectedItems()));
        }
        
        if (command.getItemsOrder() != null) {
            cv.setItemsOrder(CVSelectionMapper.toItemsOrder(command.getItemsOrder()));
        }

        cvRepository.save(cv);
        PersonalInfo personalInfo = personalInfoRepository.findByUserId(userId).orElse(null);
        return mapCv(cv, personalInfo);
    }

    @Override
    @Transactional
    public void deleteCV(String email, Long cvId) {
        Long userId = getUserId(email);
        CV cv = getCvForOwner(cvId, userId);

        educationRepository.deleteByCvId(cvId);
        experienceRepository.deleteByCvId(cvId);
        skillRepository.deleteByCvId(cvId);
        projectRepository.deleteByCvId(cvId);
        languageRepository.deleteByCvId(cvId);
        certificateRepository.deleteByCvId(cvId);

        cvRepository.delete(cv);
    }

    @Override
    @Transactional
    public EducationResponse addEducation(String email, Long cvId, AddEducationCommand command) {
        Long userId = getUserId(email);
        validateOwnership(cvId, userId);
        Education education = EducationBuilder.newEducation(cvId, command.getDegree(), command.getFieldOfStudy(), command.getStartDate())
                .universityId(command.getUniversityId())
                .endDate(command.getEndDate())
                .grade(command.getGrade())
                .description(command.getDescription())
                .build();
        educationRepository.save(education);
        return CVMapper.mapEducation(List.of(education)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public EducationResponse updateEducation(String email, Long educationId, AddEducationCommand command) {
        Long userId = getUserId(email);
        Education education = educationRepository.findById(educationId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(education.getCvId(), userId);

        if (command.getUniversityId() != null) education.setUniversityId(command.getUniversityId());
        if (command.getDegree() != null) education.setDegree(command.getDegree());
        if (command.getFieldOfStudy() != null) education.setFieldOfStudy(command.getFieldOfStudy());
        if (command.getStartDate() != null) education.setStartDate(command.getStartDate());
        education.setEndDate(command.getEndDate());
        education.setGrade(command.getGrade());
        education.setDescription(command.getDescription());

        educationRepository.save(education);
        return CVMapper.mapEducation(List.of(education)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public void deleteEducation(String email, Long educationId) {
        Long userId = getUserId(email);
        Education education = educationRepository.findById(educationId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(education.getCvId(), userId);
        educationRepository.delete(education);
    }

    @Override
    @Transactional
    public ExperienceResponse addExperience(String email, Long cvId, AddExperienceCommand command) {
        Long userId = getUserId(email);
        validateOwnership(cvId, userId);
        Experience experience = ExperienceBuilder.newExperience(cvId, command.getPosition(), command.getCompany(), command.getStartDate())
                .location(command.getLocation())
                .endDate(command.getEndDate())
                .current(Boolean.TRUE.equals(command.getIsCurrent()))
                .description(command.getDescription())
                .achievements(command.getAchievements())
                .build();
        experienceRepository.save(experience);
        return CVMapper.mapExperience(List.of(experience)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public ExperienceResponse updateExperience(String email, Long experienceId, AddExperienceCommand command) {
        Long userId = getUserId(email);
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(experience.getCvId(), userId);

        if (command.getPosition() != null) experience.setPosition(command.getPosition());
        if (command.getCompany() != null) experience.setCompany(command.getCompany());
        if (command.getLocation() != null) experience.setLocation(command.getLocation());
        if (command.getStartDate() != null) experience.setStartDate(command.getStartDate());
        experience.setEndDate(command.getEndDate());
        experience.setCurrent(Boolean.TRUE.equals(command.getIsCurrent()));
        experience.setDescription(command.getDescription());
        experience.setAchievements(command.getAchievements());

        experienceRepository.save(experience);
        return CVMapper.mapExperience(List.of(experience)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public void deleteExperience(String email, Long experienceId) {
        Long userId = getUserId(email);
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(experience.getCvId(), userId);
        experienceRepository.delete(experience);
    }

    @Override
    @Transactional
    public SkillResponse addSkill(String email, Long cvId, AddSkillCommand command) {
        Long userId = getUserId(email);
        validateOwnership(cvId, userId);
        Skill skill = SkillBuilder.newSkill(cvId, command.getName())
                .level(command.getLevel())
                .order(command.getOrder())
                .build();
        skillRepository.save(skill);
        return CVMapper.mapSkills(List.of(skill)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public SkillResponse updateSkill(String email, Long skillId, AddSkillCommand command) {
        Long userId = getUserId(email);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(skill.getCvId(), userId);

        if (command.getName() != null) skill.setName(command.getName());
        if (command.getLevel() != null) skill.setLevel(command.getLevel());
        skill.setOrder(command.getOrder());

        skillRepository.save(skill);
        return CVMapper.mapSkills(List.of(skill)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public void deleteSkill(String email, Long skillId) {
        Long userId = getUserId(email);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(skill.getCvId(), userId);
        skillRepository.delete(skill);
    }

    @Override
    @Transactional
    public ProjectResponse addProject(String email, Long cvId, AddProjectCommand command) {
        Long userId = getUserId(email);
        validateOwnership(cvId, userId);
        Project project = ProjectBuilder.newProject(cvId, command.getName())
                .description(command.getDescription())
                .githubUrl(command.getGithubUrl())
                .liveUrl(command.getLiveUrl())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .technologies(command.getTechnologies())
                .build();
        projectRepository.save(project);
        return CVMapper.mapProjects(List.of(project)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(String email, Long projectId, AddProjectCommand command) {
        Long userId = getUserId(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(project.getCvId(), userId);

        if (command.getName() != null) project.setName(command.getName());
        project.setDescription(command.getDescription());
        project.setGithubUrl(command.getGithubUrl());
        project.setLiveUrl(command.getLiveUrl());
        project.setStartDate(command.getStartDate());
        project.setEndDate(command.getEndDate());
        project.setTechnologies(command.getTechnologies());

        projectRepository.save(project);
        return CVMapper.mapProjects(List.of(project)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public void deleteProject(String email, Long projectId) {
        Long userId = getUserId(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(project.getCvId(), userId);
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public LanguageResponse addLanguage(String email, Long cvId, AddLanguageCommand command) {
        Long userId = getUserId(email);
        validateOwnership(cvId, userId);
        Language language = LanguageBuilder.newLanguage(cvId, command.getName())
                .proficiency(command.getProficiency())
                .build();
        languageRepository.save(language);
        return CVMapper.mapLanguages(List.of(language)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public LanguageResponse updateLanguage(String email, Long languageId, AddLanguageCommand command) {
        Long userId = getUserId(email);
        Language language = languageRepository.findById(languageId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(language.getCvId(), userId);

        if (command.getName() != null) language.setName(command.getName());
        language.setProficiency(command.getProficiency());

        languageRepository.save(language);
        return CVMapper.mapLanguages(List.of(language)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public void deleteLanguage(String email, Long languageId) {
        Long userId = getUserId(email);
        Language language = languageRepository.findById(languageId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(language.getCvId(), userId);
        languageRepository.delete(language);
    }

    @Override
    @Transactional
    public CertificateResponse addCertificate(String email, Long cvId, AddCertificateCommand command) {
        Long userId = getUserId(email);
        validateOwnership(cvId, userId);
        Certificate certificate = CertificateBuilder.newCertificate(cvId, command.getName())
                .issuer(command.getIssuer())
                .date(command.getDate())
                .credentialUrl(command.getCredentialUrl())
                .build();
        certificateRepository.save(certificate);
        return CVMapper.mapCertificates(List.of(certificate)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public CertificateResponse updateCertificate(String email, Long certificateId, AddCertificateCommand command) {
        Long userId = getUserId(email);
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(certificate.getCvId(), userId);

        if (command.getName() != null) certificate.setName(command.getName());
        certificate.setIssuer(command.getIssuer());
        certificate.setDate(command.getDate());
        certificate.setCredentialUrl(command.getCredentialUrl());

        certificateRepository.save(certificate);
        return CVMapper.mapCertificates(List.of(certificate)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public void deleteCertificate(String email, Long certificateId) {
        Long userId = getUserId(email);
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(SectionNotFoundException::new);
        validateOwnership(certificate.getCvId(), userId);
        certificateRepository.delete(certificate);
    }

    @Override
    public List<UniversityResponse> getUniversities() {
        return universityRepository.findAll().stream()
                .map(UniversityMapper::toResponse)
                .toList();
    }

    private CV getCvForOwner(Long cvId, Long userId) {
        CV cv = cvRepository.findById(cvId).orElseThrow(CVNotFoundException::new);
        validateOwnership(cv, userId);
        return cv;
    }

    private void validateOwnership(Long cvId, Long userId) {
        CV cv = cvRepository.findById(cvId).orElseThrow(CVNotFoundException::new);
        validateOwnership(cv, userId);
    }

    private void validateOwnership(CV cv, Long userId) {
        if (!cv.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have permission to access this CV");
        }
    }

    private void unsetExistingDefault(Long userId, Long excludeCvId) {
        Optional<CV> currentDefault = cvRepository.findDefaultByUserId(userId);
        currentDefault.ifPresent(existing -> {
            if (excludeCvId == null || !existing.getId().equals(excludeCvId)) {
                existing.setDefault(false);
                cvRepository.save(existing);
            }
        });
    }

    private CVResponse mapCv(CV cv, PersonalInfo personalInfo) {
        CVTemplate template = cv.getTemplateId() == null ? null : cvTemplateRepository.findById(cv.getTemplateId()).orElse(null);
        List<Education> educations = educationRepository.findByCvId(cv.getId());
        List<Experience> experiences = experienceRepository.findByCvId(cv.getId());
        List<Skill> skills = skillRepository.findByCvId(cv.getId());
        List<Project> projects = projectRepository.findByCvId(cv.getId());
        List<Language> languages = languageRepository.findByCvId(cv.getId());
        List<Certificate> certificates = certificateRepository.findByCvId(cv.getId());
        return CVMapper.toResponse(cv, template, personalInfo, educations, experiences, skills, projects, languages, certificates);
    }

    private Long getUserId(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(EmailNotFoundException::new);
        return user.getId();
    }
}
