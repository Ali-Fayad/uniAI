/**
 * usePersonalInfoAddActions
 *
 * Responsibility:
 * - Provide add-entry handlers for Personal Info list sections.
 * - Reset related draft inputs after successful add.
 *
 * Does NOT:
 * - Perform API calls
 * - Render UI
 */

import { useCallback } from 'react';
import type {
  PersonalInfoCertificateEntryDto,
  PersonalInfoEducationEntryDto,
  PersonalInfoExperienceEntryDto,
  PersonalInfoLanguageEntryDto,
  PersonalInfoProjectEntryDto,
  PersonalInfoSkillEntryDto,
} from '../../../types/dto';
import { createClientId } from './personalInfoUtils';

export interface UsePersonalInfoAddActionsArgs {
  setError: React.Dispatch<React.SetStateAction<string | null>>;

  universityQuery: string;
  setUniversityQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedUniversityId: number | null;
  setSelectedUniversityId: React.Dispatch<React.SetStateAction<number | null>>;
  setEducation: React.Dispatch<React.SetStateAction<PersonalInfoEducationEntryDto[]>>;

  skillQuery: string;
  setSkillQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedSkillId: number | null;
  setSelectedSkillId: React.Dispatch<React.SetStateAction<number | null>>;
  setSkills: React.Dispatch<React.SetStateAction<PersonalInfoSkillEntryDto[]>>;

  languageQuery: string;
  setLanguageQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedLanguageId: number | null;
  setSelectedLanguageId: React.Dispatch<React.SetStateAction<number | null>>;
  setLanguages: React.Dispatch<React.SetStateAction<PersonalInfoLanguageEntryDto[]>>;

  positionQuery: string;
  setPositionQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedPositionId: number | null;
  setSelectedPositionId: React.Dispatch<React.SetStateAction<number | null>>;
  experienceCompany: string;
  setExperienceCompany: React.Dispatch<React.SetStateAction<string>>;
  setExperience: React.Dispatch<React.SetStateAction<PersonalInfoExperienceEntryDto[]>>;

  projectName: string;
  setProjectName: React.Dispatch<React.SetStateAction<string>>;
  projectDescription: string;
  setProjectDescription: React.Dispatch<React.SetStateAction<string>>;
  projectRepositoryUrl: string;
  setProjectRepositoryUrl: React.Dispatch<React.SetStateAction<string>>;
  projectLiveUrl: string;
  setProjectLiveUrl: React.Dispatch<React.SetStateAction<string>>;
  setProjects: React.Dispatch<React.SetStateAction<PersonalInfoProjectEntryDto[]>>;

  certificateName: string;
  setCertificateName: React.Dispatch<React.SetStateAction<string>>;
  certificateIssuer: string;
  setCertificateIssuer: React.Dispatch<React.SetStateAction<string>>;
  certificateCredentialUrl: string;
  setCertificateCredentialUrl: React.Dispatch<React.SetStateAction<string>>;
  setCertificates: React.Dispatch<React.SetStateAction<PersonalInfoCertificateEntryDto[]>>;
}

export interface UsePersonalInfoAddActionsReturn {
  addEducation: () => void;
  addSkill: () => void;
  addLanguage: () => void;
  addExperience: () => void;
  addProject: () => void;
  addCertificate: () => void;
}

export const usePersonalInfoAddActions = (args: UsePersonalInfoAddActionsArgs): UsePersonalInfoAddActionsReturn => {
  const addEducation = useCallback(() => {
    const value = args.universityQuery.trim();
    if (!value || args.selectedUniversityId === null) {
      args.setError('Please pick a university from the list before adding.');
      return;
    }

    const id = `uni-${args.selectedUniversityId}-${createClientId()}`;

    args.setEducation((prev) => [
      ...prev,
      {
        id,
        universityId: args.selectedUniversityId,
        universityName: value,
      },
    ]);

    args.setUniversityQuery('');
    args.setSelectedUniversityId(null);
    args.setError(null);
  }, [args]);

  const addSkill = useCallback(() => {
    const value = args.skillQuery.trim();
    if (!value || args.selectedSkillId === null) {
      args.setError('Please pick a skill from the catalog list before adding.');
      return;
    }

    const skillId = String(args.selectedSkillId);
    args.setSkills((prev) => [
      ...prev,
      {
        id: `${skillId}-${createClientId()}`,
        skillId,
        name: value,
      },
    ]);

    args.setSkillQuery('');
    args.setSelectedSkillId(null);
    args.setError(null);
  }, [args]);

  const addLanguage = useCallback(() => {
    const value = args.languageQuery.trim();
    if (!value || args.selectedLanguageId === null) {
      args.setError('Please pick a language from the catalog list before adding.');
      return;
    }

    const languageId = String(args.selectedLanguageId);
    args.setLanguages((prev) => [
      ...prev,
      {
        id: `${languageId}-${createClientId()}`,
        languageId,
        name: value,
      },
    ]);

    args.setLanguageQuery('');
    args.setSelectedLanguageId(null);
    args.setError(null);
  }, [args]);

  const addExperience = useCallback(() => {
    const position = args.positionQuery.trim();
    const company = args.experienceCompany.trim();

    if (!position || args.selectedPositionId === null) {
      args.setError('Please pick a position from the catalog list before adding.');
      return;
    }

    const positionId = String(args.selectedPositionId);

    args.setExperience((prev) => [
      ...prev,
      {
        id: `${positionId}-${createClientId()}`,
        positionId,
        position,
        company,
      },
    ]);

    args.setPositionQuery('');
    args.setExperienceCompany('');
    args.setSelectedPositionId(null);
    args.setError(null);
  }, [args]);

  const addProject = useCallback(() => {
    const name = args.projectName.trim();
    if (!name) {
      args.setError('Project name is required.');
      return;
    }

    args.setProjects((prev) => [
      ...prev,
      {
        id: createClientId(),
        name,
        description: args.projectDescription.trim(),
        repositoryUrl: args.projectRepositoryUrl.trim(),
        liveUrl: args.projectLiveUrl.trim(),
      },
    ]);

    args.setProjectName('');
    args.setProjectDescription('');
    args.setProjectRepositoryUrl('');
    args.setProjectLiveUrl('');
    args.setError(null);
  }, [args]);

  const addCertificate = useCallback(() => {
    const name = args.certificateName.trim();
    if (!name) {
      args.setError('Certificate name is required.');
      return;
    }

    args.setCertificates((prev) => [
      ...prev,
      {
        id: createClientId(),
        name,
        issuer: args.certificateIssuer.trim(),
        credentialUrl: args.certificateCredentialUrl.trim(),
      },
    ]);

    args.setCertificateName('');
    args.setCertificateIssuer('');
    args.setCertificateCredentialUrl('');
    args.setError(null);
  }, [args]);

  return { addEducation, addSkill, addLanguage, addExperience, addProject, addCertificate };
};
