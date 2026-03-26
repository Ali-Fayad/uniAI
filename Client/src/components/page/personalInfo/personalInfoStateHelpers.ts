/**
 * personalInfoStateHelpers
 *
 * Responsibility:
 * - Map API Personal Info responses into UI state.
 * - Validate Personal Info state before saving.
 * - Provide snapshot serialization for dirty tracking.
 *
 * Does NOT:
 * - Own React state
 * - Perform API calls
 */

import type {
  PersonalInfoCertificateEntryDto,
  PersonalInfoEducationEntryDto,
  PersonalInfoExperienceEntryDto,
  PersonalInfoLanguageEntryDto,
  PersonalInfoProjectEntryDto,
  PersonalInfoResponseDto,
  PersonalInfoSkillEntryDto,
} from '../../../types/dto';
import type { BasicFormState } from './personalInfoTypes';
import { createClientId, normalizeOptionId } from './personalInfoUtils';

export interface PersonalInfoState {
  form: BasicFormState;
  education: PersonalInfoEducationEntryDto[];
  skills: PersonalInfoSkillEntryDto[];
  languages: PersonalInfoLanguageEntryDto[];
  experience: PersonalInfoExperienceEntryDto[];
  projects: PersonalInfoProjectEntryDto[];
  certificates: PersonalInfoCertificateEntryDto[];
}

export const createEmptyPersonalInfoFormState = (): BasicFormState => ({
  phone: '',
  address: '',
  jobTitle: '',
  company: '',
  linkedin: '',
  github: '',
  portfolio: '',
  summary: '',
});

export const mapPersonalInfoResponseToState = (data: PersonalInfoResponseDto): PersonalInfoState => {
  const education = (data.education ?? []).map((item) => ({
    id: item.id || createClientId(),
    universityId: item.universityId ?? null,
    universityName: item.universityName ?? '',
  }));

  const skills = (data.skills ?? []).map((item) => ({
    id: item.id || createClientId(),
    skillId: item.skillId || normalizeOptionId('skill', item.name),
    name: item.name || '',
  }));

  const languages = (data.languages ?? []).map((item) => ({
    id: item.id || createClientId(),
    languageId: item.languageId || normalizeOptionId('language', item.name),
    name: item.name || '',
  }));

  const experience = (data.experience ?? []).map((item) => ({
    id: item.id || createClientId(),
    positionId: item.positionId || createClientId(),
    position: item.position || '',
    company: item.company || '',
  }));

  const projects = (data.projects ?? []).map((item) => ({
    id: item.id || createClientId(),
    name: item.name || '',
    description: item.description || '',
    repositoryUrl: item.repositoryUrl || '',
    liveUrl: item.liveUrl || '',
  }));

  const certificates = (data.certificates ?? []).map((item) => ({
    id: item.id || createClientId(),
    name: item.name || '',
    issuer: item.issuer || '',
    credentialUrl: item.credentialUrl || '',
  }));

  const form: BasicFormState = {
    phone: data.phone ?? '',
    address: data.address ?? '',
    jobTitle: data.jobTitle ?? '',
    company: data.company ?? '',
    linkedin: data.linkedin ?? '',
    github: data.github ?? '',
    portfolio: data.portfolio ?? '',
    summary: data.summary ?? '',
  };

  return { form, education, skills, languages, experience, projects, certificates };
};

export const personalInfoStateToSnapshot = (state: PersonalInfoState): string => {
  return JSON.stringify(state);
};

export const validatePersonalInfoState = (state: PersonalInfoState): string | null => {
  if (state.form.phone && !isValidPhoneNumber(state.form.phone)) {
    return "Phone number invalid. Must be in format '+TotalDigits RegionDigits' (e.g. +1 5551234567)";
  }

  const invalidEducation = state.education.some((item) => !item.universityName.trim() || !item.universityId);
  if (invalidEducation) {
    return 'Education entries must include a selected university.';
  }

  const invalidSkills = state.skills.some((item) => !item.name.trim() || !item.skillId);
  if (invalidSkills) {
    return 'Skills entries must include a selected skill.';
  }

  const invalidLanguages = state.languages.some((item) => !item.name.trim() || !item.languageId);
  if (invalidLanguages) {
    return 'Language entries must include a selected language.';
  }

  const invalidExperience = state.experience.some((item) => !item.position.trim() || !item.positionId);
  if (invalidExperience) {
    return 'Experience entries must include a selected position.';
  }

  const invalidProjects = state.projects.some((item) => !item.name.trim());
  if (invalidProjects) {
    return 'Project name is required for every project entry.';
  }

  const invalidCertificates = state.certificates.some((item) => !item.name.trim());
  if (invalidCertificates) {
    return 'Certificate name is required for every certificate entry.';
  }

  return null;
};
