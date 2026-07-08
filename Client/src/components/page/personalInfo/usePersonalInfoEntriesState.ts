/**
 * usePersonalInfoEntriesState
 *
 * Responsibility:
 * - Own array-based Personal Info entries (education, skills, etc.).
 *
 * Does NOT:
 * - Perform API calls
 * - Validate or map DTOs
 */

import { useState } from 'react';
import type {
  PersonalInfoCertificateEntryDto,
  PersonalInfoEducationEntryDto,
  PersonalInfoExperienceEntryDto,
  PersonalInfoLanguageEntryDto,
  PersonalInfoProjectEntryDto,
  PersonalInfoSkillEntryDto,
} from '../../../types/dto';

export interface UsePersonalInfoEntriesStateReturn {
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
}

export const usePersonalInfoEntriesState = (): UsePersonalInfoEntriesStateReturn => {
  const [education, setEducation] = useState<PersonalInfoEducationEntryDto[]>([]);
  const [skills, setSkills] = useState<PersonalInfoSkillEntryDto[]>([]);
  const [languages, setLanguages] = useState<PersonalInfoLanguageEntryDto[]>([]);
  const [experience, setExperience] = useState<PersonalInfoExperienceEntryDto[]>([]);
  const [projects, setProjects] = useState<PersonalInfoProjectEntryDto[]>([]);
  const [certificates, setCertificates] = useState<PersonalInfoCertificateEntryDto[]>([]);

  return {
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
  };
};
