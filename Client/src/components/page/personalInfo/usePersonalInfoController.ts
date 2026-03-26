/**
 * usePersonalInfoController
 *
 * Responsibility:
 * - Own Personal Info page state, derived flags (dirty/loading), and side effects.
 * - Load/save Personal Info through `cvService`.
 * - Provide handlers used by UI sections (add/edit/reorder/remove entries).
 *
 * Does NOT:
 * - Render UI
 * - Define layout or styling
 */

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cvService } from '../../../services/cv';
import { ROUTES } from '../../../router';
import type {
  PersonalInfoCertificateEntryDto,
  PersonalInfoEducationEntryDto,
  PersonalInfoExperienceEntryDto,
  PersonalInfoLanguageEntryDto,
  PersonalInfoProjectEntryDto,
  PersonalInfoResponseDto,
  PersonalInfoSkillEntryDto,
} from '../../../types/dto';
import { useLanguages } from '../../../hooks/useLanguages';
import { usePositions } from '../../../hooks/usePositions';
import { useSkills } from '../../../hooks/useSkills';
import { useUniversities } from '../../../hooks/useUniversities';
import type { BasicFormState } from './personalInfoTypes';
import { mapPersonalInfoResponseToState, validatePersonalInfoState } from './personalInfoStateHelpers';
import { usePersonalInfoAddActions } from './usePersonalInfoAddActions';
import { usePersonalInfoDirtyTracking } from './usePersonalInfoDirtyTracking';
import { usePersonalInfoDraftState } from './usePersonalInfoDraftState';
import { usePersonalInfoEntriesState } from './usePersonalInfoEntriesState';
import { usePersonalInfoFormState } from './usePersonalInfoFormState';

export interface UsePersonalInfoControllerArgs {
  fromOnboarding: boolean;
}

export interface UsePersonalInfoControllerReturn {
  fromOnboarding: boolean;

  isPageLoading: boolean;
  isSaving: boolean;
  isDirty: boolean;
  error: string | null;
  saveToast: string | null;

  form: BasicFormState;
  setField: (field: keyof BasicFormState, value: string) => void;

  education: PersonalInfoEducationEntryDto[];
  setEducation: React.Dispatch<React.SetStateAction<PersonalInfoEducationEntryDto[]>>;
  skills: PersonalInfoSkillEntryDto[];
  setSkills: React.Dispatch<React.SetStateAction<PersonalInfoSkillEntryDto[]>>;
  languages: PersonalInfoLanguageEntryDto[];
  setLanguages: React.Dispatch<React.SetStateAction<PersonalInfoLanguageEntryDto[]>>;
  experience: PersonalInfoExperienceEntryDto[];
  setExperience: React.Dispatch<React.SetStateAction<PersonalInfoExperienceEntryDto[]>>;
  projects: PersonalInfoProjectEntryDto[];
  setProjects: React.Dispatch<React.SetStateAction<PersonalInfoProjectEntryDto[]>>;
  certificates: PersonalInfoCertificateEntryDto[];
  setCertificates: React.Dispatch<React.SetStateAction<PersonalInfoCertificateEntryDto[]>>;

  universityQuery: string;
  setUniversityQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedUniversityId: number | null;
  setSelectedUniversityId: React.Dispatch<React.SetStateAction<number | null>>;
  universitySuggestions: Array<{ id: number; name: string; acronym?: string | null }>;
  isUniversitiesLoading: boolean;

  skillQuery: string;
  setSkillQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedSkillId: number | null;
  setSelectedSkillId: React.Dispatch<React.SetStateAction<number | null>>;
  skillSuggestions: Array<{ id: number; name: string }>;
  isSkillsLoading: boolean;

  languageQuery: string;
  setLanguageQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedLanguageId: number | null;
  setSelectedLanguageId: React.Dispatch<React.SetStateAction<number | null>>;
  languageSuggestions: Array<{ id: number; name: string }>;
  isLanguagesLoading: boolean;

  positionQuery: string;
  setPositionQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedPositionId: number | null;
  setSelectedPositionId: React.Dispatch<React.SetStateAction<number | null>>;
  experienceCompany: string;
  setExperienceCompany: React.Dispatch<React.SetStateAction<string>>;
  positionSuggestions: Array<{ id: number; name: string }>;
  isPositionsLoading: boolean;

  projectName: string;
  setProjectName: React.Dispatch<React.SetStateAction<string>>;
  projectDescription: string;
  setProjectDescription: React.Dispatch<React.SetStateAction<string>>;
  projectRepositoryUrl: string;
  setProjectRepositoryUrl: React.Dispatch<React.SetStateAction<string>>;
  projectLiveUrl: string;
  setProjectLiveUrl: React.Dispatch<React.SetStateAction<string>>;

  certificateName: string;
  setCertificateName: React.Dispatch<React.SetStateAction<string>>;
  certificateIssuer: string;
  setCertificateIssuer: React.Dispatch<React.SetStateAction<string>>;
  certificateCredentialUrl: string;
  setCertificateCredentialUrl: React.Dispatch<React.SetStateAction<string>>;

  editingEducationId: string | null;
  setEditingEducationId: React.Dispatch<React.SetStateAction<string | null>>;
  editingEducationValue: string;
  setEditingEducationValue: React.Dispatch<React.SetStateAction<string>>;

  editingSkillId: string | null;
  setEditingSkillId: React.Dispatch<React.SetStateAction<string | null>>;
  editingSkillValue: string;
  setEditingSkillValue: React.Dispatch<React.SetStateAction<string>>;

  editingLanguageId: string | null;
  setEditingLanguageId: React.Dispatch<React.SetStateAction<string | null>>;
  editingLanguageValue: string;
  setEditingLanguageValue: React.Dispatch<React.SetStateAction<string>>;

  editingExperienceId: string | null;
  setEditingExperienceId: React.Dispatch<React.SetStateAction<string | null>>;
  editingExperiencePosition: string;
  setEditingExperiencePosition: React.Dispatch<React.SetStateAction<string>>;
  editingExperienceCompany: string;
  setEditingExperienceCompany: React.Dispatch<React.SetStateAction<string>>;

  editingProjectId: string | null;
  setEditingProjectId: React.Dispatch<React.SetStateAction<string | null>>;
  editingProjectName: string;
  setEditingProjectName: React.Dispatch<React.SetStateAction<string>>;
  editingProjectDescription: string;
  setEditingProjectDescription: React.Dispatch<React.SetStateAction<string>>;

  editingCertificateId: string | null;
  setEditingCertificateId: React.Dispatch<React.SetStateAction<string | null>>;
  editingCertificateName: string;
  setEditingCertificateName: React.Dispatch<React.SetStateAction<string>>;
  editingCertificateIssuer: string;
  setEditingCertificateIssuer: React.Dispatch<React.SetStateAction<string>>;

  addEducation: () => void;
  addSkill: () => void;
  addLanguage: () => void;
  addExperience: () => void;
  addProject: () => void;
  addCertificate: () => void;

  confirmNavigationIfDirty: (to: string) => void;
  saveChanges: () => Promise<void>;
  setError: React.Dispatch<React.SetStateAction<string | null>>;
}

export const usePersonalInfoController = ({ fromOnboarding }: UsePersonalInfoControllerArgs): UsePersonalInfoControllerReturn => {
  const navigate = useNavigate();

  const [isPageLoading, setIsPageLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saveToast, setSaveToast] = useState<string | null>(null);

  const { form, setForm, setField } = usePersonalInfoFormState();
  const {
    education,
    setEducation,
    skills,
    setSkills,
    languages,
    setLanguages,
    experience,
    setExperience,
    projects,
    setProjects,
    certificates,
    setCertificates,
  } = usePersonalInfoEntriesState();

  const drafts = usePersonalInfoDraftState();

  const personalInfoState = {
    form,
    education,
    skills,
    languages,
    experience,
    projects,
    certificates,
  };

  const { isDirty, markClean } = usePersonalInfoDirtyTracking(personalInfoState);

  const { items: universitySuggestions, isLoading: isUniversitiesLoading } = useUniversities(drafts.universityQuery);
  const { items: skillSuggestions, isLoading: isSkillsLoading } = useSkills(drafts.skillQuery);
  const { items: languageSuggestions, isLoading: isLanguagesLoading } = useLanguages(drafts.languageQuery);
  const { items: positionSuggestions, isLoading: isPositionsLoading } = usePositions(drafts.positionQuery);

  const applyResponseToState = (data: PersonalInfoResponseDto) => {
    const mapped = mapPersonalInfoResponseToState(data);

    setForm(mapped.form);
    setEducation(mapped.education);
    setSkills(mapped.skills);
    setLanguages(mapped.languages);
    setExperience(mapped.experience);
    setProjects(mapped.projects);
    setCertificates(mapped.certificates);

    markClean(mapped);
  };

  useEffect(() => {
    const loadPersonalInfo = async () => {
      setIsPageLoading(true);
      setError(null);
      try {
        const response = await cvService.getPersonalInfo();
        applyResponseToState(response);
      } catch {
        setError('Failed to load your personal information. Please try again.');
      } finally {
        setIsPageLoading(false);
      }
    };

    void loadPersonalInfo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!saveToast) {
      return;
    }

    const timeout = setTimeout(() => setSaveToast(null), 3000);
    return () => clearTimeout(timeout);
  }, [saveToast]);

  const { addEducation, addSkill, addLanguage, addExperience, addProject, addCertificate } = usePersonalInfoAddActions({
    setError,

    universityQuery: drafts.universityQuery,
    setUniversityQuery: drafts.setUniversityQuery,
    selectedUniversityId: drafts.selectedUniversityId,
    setSelectedUniversityId: drafts.setSelectedUniversityId,
    setEducation,

    skillQuery: drafts.skillQuery,
    setSkillQuery: drafts.setSkillQuery,
    selectedSkillId: drafts.selectedSkillId,
    setSelectedSkillId: drafts.setSelectedSkillId,
    setSkills,

    languageQuery: drafts.languageQuery,
    setLanguageQuery: drafts.setLanguageQuery,
    selectedLanguageId: drafts.selectedLanguageId,
    setSelectedLanguageId: drafts.setSelectedLanguageId,
    setLanguages,

    positionQuery: drafts.positionQuery,
    setPositionQuery: drafts.setPositionQuery,
    selectedPositionId: drafts.selectedPositionId,
    setSelectedPositionId: drafts.setSelectedPositionId,
    experienceCompany: drafts.experienceCompany,
    setExperienceCompany: drafts.setExperienceCompany,
    setExperience,

    projectName: drafts.projectName,
    setProjectName: drafts.setProjectName,
    projectDescription: drafts.projectDescription,
    setProjectDescription: drafts.setProjectDescription,
    projectRepositoryUrl: drafts.projectRepositoryUrl,
    setProjectRepositoryUrl: drafts.setProjectRepositoryUrl,
    projectLiveUrl: drafts.projectLiveUrl,
    setProjectLiveUrl: drafts.setProjectLiveUrl,
    setProjects,

    certificateName: drafts.certificateName,
    setCertificateName: drafts.setCertificateName,
    certificateIssuer: drafts.certificateIssuer,
    setCertificateIssuer: drafts.setCertificateIssuer,
    certificateCredentialUrl: drafts.certificateCredentialUrl,
    setCertificateCredentialUrl: drafts.setCertificateCredentialUrl,
    setCertificates,
  });

  const confirmNavigationIfDirty = (to: string) => {
    if (isDirty && !window.confirm('You have unsaved changes. Leave this page?')) {
      return;
    }
    navigate(to);
  };

  const saveChanges = async () => {
    const validationError = validatePersonalInfoState(personalInfoState);
    if (validationError) {
      setError(validationError);
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      const response = await cvService.updatePersonalInfo({
        ...form,
        education,
        skills,
        languages,
        experience,
        projects,
        certificates,
      });

      applyResponseToState(response);

      if (fromOnboarding) {
        navigate(ROUTES.CHAT, { replace: true });
        return;
      }

      setSaveToast('Personal info updated successfully.');
    } catch {
      setError('Failed to save your personal information. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  return {
    fromOnboarding,

    isPageLoading,
    isSaving,
    isDirty,
    error,
    saveToast,

    form,
    setField,

    education,
    setEducation,
    skills,
    setSkills,
    languages,
    setLanguages,
    experience,
    setExperience,
    projects,
    setProjects,
    certificates,
    setCertificates,

    universityQuery: drafts.universityQuery,
    setUniversityQuery: drafts.setUniversityQuery,
    selectedUniversityId: drafts.selectedUniversityId,
    setSelectedUniversityId: drafts.setSelectedUniversityId,
    universitySuggestions,
    isUniversitiesLoading,

    skillQuery: drafts.skillQuery,
    setSkillQuery: drafts.setSkillQuery,
    selectedSkillId: drafts.selectedSkillId,
    setSelectedSkillId: drafts.setSelectedSkillId,
    skillSuggestions,
    isSkillsLoading,

    languageQuery: drafts.languageQuery,
    setLanguageQuery: drafts.setLanguageQuery,
    selectedLanguageId: drafts.selectedLanguageId,
    setSelectedLanguageId: drafts.setSelectedLanguageId,
    languageSuggestions,
    isLanguagesLoading,

    positionQuery: drafts.positionQuery,
    setPositionQuery: drafts.setPositionQuery,
    selectedPositionId: drafts.selectedPositionId,
    setSelectedPositionId: drafts.setSelectedPositionId,
    experienceCompany: drafts.experienceCompany,
    setExperienceCompany: drafts.setExperienceCompany,
    positionSuggestions,
    isPositionsLoading,

    projectName: drafts.projectName,
    setProjectName: drafts.setProjectName,
    projectDescription: drafts.projectDescription,
    setProjectDescription: drafts.setProjectDescription,
    projectRepositoryUrl: drafts.projectRepositoryUrl,
    setProjectRepositoryUrl: drafts.setProjectRepositoryUrl,
    projectLiveUrl: drafts.projectLiveUrl,
    setProjectLiveUrl: drafts.setProjectLiveUrl,

    certificateName: drafts.certificateName,
    setCertificateName: drafts.setCertificateName,
    certificateIssuer: drafts.certificateIssuer,
    setCertificateIssuer: drafts.setCertificateIssuer,
    certificateCredentialUrl: drafts.certificateCredentialUrl,
    setCertificateCredentialUrl: drafts.setCertificateCredentialUrl,

    editingEducationId: drafts.editingEducationId,
    setEditingEducationId: drafts.setEditingEducationId,
    editingEducationValue: drafts.editingEducationValue,
    setEditingEducationValue: drafts.setEditingEducationValue,

    editingSkillId: drafts.editingSkillId,
    setEditingSkillId: drafts.setEditingSkillId,
    editingSkillValue: drafts.editingSkillValue,
    setEditingSkillValue: drafts.setEditingSkillValue,

    editingLanguageId: drafts.editingLanguageId,
    setEditingLanguageId: drafts.setEditingLanguageId,
    editingLanguageValue: drafts.editingLanguageValue,
    setEditingLanguageValue: drafts.setEditingLanguageValue,

    editingExperienceId: drafts.editingExperienceId,
    setEditingExperienceId: drafts.setEditingExperienceId,
    editingExperiencePosition: drafts.editingExperiencePosition,
    setEditingExperiencePosition: drafts.setEditingExperiencePosition,
    editingExperienceCompany: drafts.editingExperienceCompany,
    setEditingExperienceCompany: drafts.setEditingExperienceCompany,

    editingProjectId: drafts.editingProjectId,
    setEditingProjectId: drafts.setEditingProjectId,
    editingProjectName: drafts.editingProjectName,
    setEditingProjectName: drafts.setEditingProjectName,
    editingProjectDescription: drafts.editingProjectDescription,
    setEditingProjectDescription: drafts.setEditingProjectDescription,

    editingCertificateId: drafts.editingCertificateId,
    setEditingCertificateId: drafts.setEditingCertificateId,
    editingCertificateName: drafts.editingCertificateName,
    setEditingCertificateName: drafts.setEditingCertificateName,
    editingCertificateIssuer: drafts.editingCertificateIssuer,
    setEditingCertificateIssuer: drafts.setEditingCertificateIssuer,

    addEducation,
    addSkill,
    addLanguage,
    addExperience,
    addProject,
    addCertificate,

    confirmNavigationIfDirty,
    saveChanges,
    setError,
  };
};
