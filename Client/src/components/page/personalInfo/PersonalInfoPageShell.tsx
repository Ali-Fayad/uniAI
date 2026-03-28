/**
 * PersonalInfoPageShell
 *
 * Responsibility:
 * - Render the Personal Info page layout by composing extracted section components.
 *
 * Does NOT:
 * - Own business logic (handled by `usePersonalInfoController`)
 */

import React from 'react';
import { ROUTES } from '../../../router';
import LoadingSpinner from '../../common/LoadingSpinner';
import BasicInformationSection from './sections/BasicInformationSection';
import BioSection from './sections/BioSection';
import CertificatesSection from './sections/CertificatesSection';
import EducationSection from './sections/EducationSection';
import ExperienceSection from './sections/ExperienceSection';
import LanguagesSection from './sections/LanguagesSection';
import ProjectsSection from './sections/ProjectsSection';
import SkillsSection from './sections/SkillsSection';
import SocialWebSection from './sections/SocialWebSection';
import PersonalInfoFooterActions from './PersonalInfoFooterActions';
import type { UsePersonalInfoControllerReturn } from './usePersonalInfoController';

export interface PersonalInfoPageShellProps {
  controller: UsePersonalInfoControllerReturn;
}

const PersonalInfoPageShell: React.FC<PersonalInfoPageShellProps> = ({ controller }) => {
  if (controller.isPageLoading) {
    return (
      <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] px-4 py-10">
        <LoadingSpinner text="Loading personal info..." />
      </div>
    );
  }

  return (
    <main className="flex-grow py-8 px-4 sm:px-6 lg:px-8 bg-[var(--color-background)]">
      {controller.saveToast && (
        <div className="fixed right-4 top-20 z-50 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3 text-sm text-[var(--color-textPrimary)] shadow-lg">
          {controller.saveToast}
        </div>
      )}

      <div className="max-w-4xl mx-auto space-y-6">
        <div className="flex flex-col gap-2">
          <h1 className="text-3xl sm:text-4xl font-bold text-[var(--color-textPrimary)]">Personal Info</h1>
          <p className="text-[var(--color-textSecondary)]">
            Add your profile details to personalize recommendations and CV support.
          </p>
        </div>

        {controller.error && (
          <div className="rounded-md border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-3 text-sm text-[var(--color-textPrimary)]">
            {controller.error}
          </div>
        )}

        <BasicInformationSection form={controller.form} setField={controller.setField} />

        <SocialWebSection form={controller.form} setField={controller.setField} />

        <BioSection form={controller.form} setField={controller.setField} />

        <EducationSection
          education={controller.education}
          setEducation={controller.setEducation}
          universityQuery={controller.universityQuery}
          setUniversityQuery={controller.setUniversityQuery}
          selectedUniversityId={controller.selectedUniversityId}
          setSelectedUniversityId={controller.setSelectedUniversityId}
          isUniversitiesLoading={controller.isUniversitiesLoading}
          universitySuggestions={controller.universitySuggestions}
          addEducation={controller.addEducation}
          setError={controller.setError}
        />

        <SkillsSection
          skills={controller.skills}
          setSkills={controller.setSkills}
          skillQuery={controller.skillQuery}
          setSkillQuery={controller.setSkillQuery}
          setSelectedSkillId={controller.setSelectedSkillId}
          isSkillsLoading={controller.isSkillsLoading}
          skillSuggestions={controller.skillSuggestions}
          addSkill={controller.addSkill}
        />

        <LanguagesSection
          languages={controller.languages}
          setLanguages={controller.setLanguages}
          languageQuery={controller.languageQuery}
          setLanguageQuery={controller.setLanguageQuery}
          setSelectedLanguageId={controller.setSelectedLanguageId}
          isLanguagesLoading={controller.isLanguagesLoading}
          languageSuggestions={controller.languageSuggestions}
          addLanguage={controller.addLanguage}
        />

        <ExperienceSection
          experience={controller.experience}
          setExperience={controller.setExperience}
          positionQuery={controller.positionQuery}
          setPositionQuery={controller.setPositionQuery}
          setSelectedPositionId={controller.setSelectedPositionId}
          experienceCompany={controller.experienceCompany}
          setExperienceCompany={controller.setExperienceCompany}
          isPositionsLoading={controller.isPositionsLoading}
          positionSuggestions={controller.positionSuggestions}
          addExperience={controller.addExperience}
        />

        <ProjectsSection
          projectName={controller.projectName}
          setProjectName={controller.setProjectName}
          projectDescription={controller.projectDescription}
          setProjectDescription={controller.setProjectDescription}
          projectRepositoryUrl={controller.projectRepositoryUrl}
          setProjectRepositoryUrl={controller.setProjectRepositoryUrl}
          projectLiveUrl={controller.projectLiveUrl}
          setProjectLiveUrl={controller.setProjectLiveUrl}
          addProject={controller.addProject}
          projects={controller.projects}
          setProjects={controller.setProjects}
        />

        <CertificatesSection
          certificateName={controller.certificateName}
          setCertificateName={controller.setCertificateName}
          certificateIssuer={controller.certificateIssuer}
          setCertificateIssuer={controller.setCertificateIssuer}
          certificateCredentialUrl={controller.certificateCredentialUrl}
          setCertificateCredentialUrl={controller.setCertificateCredentialUrl}
          addCertificate={controller.addCertificate}
          certificates={controller.certificates}
          setCertificates={controller.setCertificates}
        />

        <PersonalInfoFooterActions
          fromOnboarding={controller.fromOnboarding}
          isDirty={controller.isDirty}
          isSaving={controller.isSaving}
          onSkip={() => controller.confirmNavigationIfDirty(ROUTES.CHAT)}
          onBack={() => controller.confirmNavigationIfDirty(ROUTES.SETTINGS)}
          onSave={() => void controller.saveChanges()}
        />
      </div>
    </main>
  );
};

export default PersonalInfoPageShell;
