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

  const [editingEducationId, setEditingEducationId] = useState<string | null>(null);
  const [editingEducationValue, setEditingEducationValue] = useState('');

  const [editingSkillId, setEditingSkillId] = useState<string | null>(null);
  const [editingSkillValue, setEditingSkillValue] = useState('');

  const [editingLanguageId, setEditingLanguageId] = useState<string | null>(null);
  const [editingLanguageValue, setEditingLanguageValue] = useState('');

  const [editingExperienceId, setEditingExperienceId] = useState<string | null>(null);
  const [editingExperiencePosition, setEditingExperiencePosition] = useState('');
  const [editingExperienceCompany, setEditingExperienceCompany] = useState('');

  const [editingProjectId, setEditingProjectId] = useState<string | null>(null);
  const [editingProjectName, setEditingProjectName] = useState('');
  const [editingProjectDescription, setEditingProjectDescription] = useState('');

  const [editingCertificateId, setEditingCertificateId] = useState<string | null>(null);
  const [editingCertificateName, setEditingCertificateName] = useState('');
  const [editingCertificateIssuer, setEditingCertificateIssuer] = useState('');

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

    editingEducationId,
    setEditingEducationId,
    editingEducationValue,
    setEditingEducationValue,

    editingSkillId,
    setEditingSkillId,
    editingSkillValue,
    setEditingSkillValue,

    editingLanguageId,
    setEditingLanguageId,
    editingLanguageValue,
    setEditingLanguageValue,

    editingExperienceId,
    setEditingExperienceId,
    editingExperiencePosition,
    setEditingExperiencePosition,
    editingExperienceCompany,
    setEditingExperienceCompany,

    editingProjectId,
    setEditingProjectId,
    editingProjectName,
    setEditingProjectName,
    editingProjectDescription,
    setEditingProjectDescription,

    editingCertificateId,
    setEditingCertificateId,
    editingCertificateName,
    setEditingCertificateName,
    editingCertificateIssuer,
    setEditingCertificateIssuer,
  };
};
