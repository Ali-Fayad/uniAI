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
import { useNotification } from '../../../hooks/useNotification';
import { usePersonalInfoAddActions } from './usePersonalInfoAddActions';
import { usePersonalInfoDirtyTracking } from './usePersonalInfoDirtyTracking';
import { usePersonalInfoDraftState } from './usePersonalInfoDraftState';
import { usePersonalInfoEntriesState } from './usePersonalInfoEntriesState';
import { usePersonalInfoFormState } from './usePersonalInfoFormState';
import { mapPersonalInfoStateToUpdateDto } from './personalInfoApiMapper';

export interface UsePersonalInfoControllerArgs {
  fromOnboarding: boolean;
}

export interface UsePersonalInfoControllerReturn {
  fromOnboarding: boolean;

  isPageLoading: boolean;
  isSaving: boolean;
  isDirty: boolean;
  error: string | null;
  missingFields: string[];
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
  const { showNotification } = useNotification();

  const [isPageLoading, setIsPageLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saveToast, setSaveToast] = useState<string | null>(null);
  const [missingFields, setMissingFields] = useState<string[]>([]);

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
    const validation = validatePersonalInfoState(personalInfoState);
    if (validation.error) {
      setError(validation.error);
      setMissingFields(validation.missingFields);
      
      const message = validation.missingFields.length > 0
        ? `Missing required fields: ${validation.missingFields.map(f => f.charAt(0).toUpperCase() + f.slice(1)).join(', ')}`
        : validation.error;
        
      showNotification({
        type: 'error',
        message: message,
        duration: 5000,
        showCloseButton: true
      });
      return;
    }
    setMissingFields([]);

    setIsSaving(true);
    setError(null);

    try {
      const payload = mapPersonalInfoStateToUpdateDto(personalInfoState);

      const response = await cvService.updatePersonalInfo(payload);

      applyResponseToState(response);

      if (fromOnboarding) {
        navigate(ROUTES.CHAT, { replace: true });
        showNotification({ type: 'success', message: 'Profile completed successfully!' });
        return;
      }

      setSaveToast('Personal info updated successfully.');
      showNotification({ type: 'success', message: 'Personal info updated successfully.' });
    } catch {
      setError('Failed to save your personal information. Please try again.');
      showNotification({
        type: 'error',
        message: 'Failed to save your personal information. Please try again.',
        duration: 5000,
        showCloseButton: true
      });
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
    missingFields,
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
