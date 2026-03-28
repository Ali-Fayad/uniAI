/**
 * usePersonalInfoDraftState
 *
 * Responsibility:
 * - Own transient UI draft state for Personal Info sections (queries, selected IDs, input drafts, editing fields).
 *
 * Does NOT:
 * - Perform API calls
 * - Persist data
 */

import { useState } from 'react';

export interface UsePersonalInfoDraftStateReturn {
  universityQuery: string;
  setUniversityQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedUniversityId: number | null;
  setSelectedUniversityId: React.Dispatch<React.SetStateAction<number | null>>;

  skillQuery: string;
  setSkillQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedSkillId: number | null;
  setSelectedSkillId: React.Dispatch<React.SetStateAction<number | null>>;

  languageQuery: string;
  setLanguageQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedLanguageId: number | null;
  setSelectedLanguageId: React.Dispatch<React.SetStateAction<number | null>>;

  positionQuery: string;
  setPositionQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedPositionId: number | null;
  setSelectedPositionId: React.Dispatch<React.SetStateAction<number | null>>;
  experienceCompany: string;
  setExperienceCompany: React.Dispatch<React.SetStateAction<string>>;

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






}

export const usePersonalInfoDraftState = (): UsePersonalInfoDraftStateReturn => {
  const [universityQuery, setUniversityQuery] = useState('');
  const [selectedUniversityId, setSelectedUniversityId] = useState<number | null>(null);

  const [skillQuery, setSkillQuery] = useState('');
  const [selectedSkillId, setSelectedSkillId] = useState<number | null>(null);

  const [languageQuery, setLanguageQuery] = useState('');
  const [selectedLanguageId, setSelectedLanguageId] = useState<number | null>(null);

  const [positionQuery, setPositionQuery] = useState('');
  const [selectedPositionId, setSelectedPositionId] = useState<number | null>(null);
  const [experienceCompany, setExperienceCompany] = useState('');

  const [projectName, setProjectName] = useState('');
  const [projectDescription, setProjectDescription] = useState('');
  const [projectRepositoryUrl, setProjectRepositoryUrl] = useState('');
  const [projectLiveUrl, setProjectLiveUrl] = useState('');

  const [certificateName, setCertificateName] = useState('');
  const [certificateIssuer, setCertificateIssuer] = useState('');
  const [certificateCredentialUrl, setCertificateCredentialUrl] = useState('');







  return {
    universityQuery,
    setUniversityQuery,
    selectedUniversityId,
    setSelectedUniversityId,

    skillQuery,
    setSkillQuery,
    selectedSkillId,
    setSelectedSkillId,

    languageQuery,
    setLanguageQuery,
    selectedLanguageId,
    setSelectedLanguageId,

    positionQuery,
    setPositionQuery,
    selectedPositionId,
    setSelectedPositionId,
    experienceCompany,
    setExperienceCompany,

    projectName,
    setProjectName,
    projectDescription,
    setProjectDescription,
    projectRepositoryUrl,
    setProjectRepositoryUrl,
    projectLiveUrl,
    setProjectLiveUrl,

    certificateName,
    setCertificateName,
    certificateIssuer,
    setCertificateIssuer,
    certificateCredentialUrl,
    setCertificateCredentialUrl,






  };
};
