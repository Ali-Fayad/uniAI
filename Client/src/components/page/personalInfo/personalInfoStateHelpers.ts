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

import { isValidPhoneNumber } from "../../../lib/utils";
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


export interface ValidationResult {
  error: string | null;
  missingFields: string[];
}


export interface ValidationResult {
  error: string | null;
  missingFields: string[];
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

export const validatePersonalInfoState = (state: PersonalInfoState): ValidationResult => {
  const missingFields: string[] = [];
  
  const isPhoneValid = state.form.phone && state.form.phone.trim().length > 0;
  const isAddressValid = state.form.address && state.form.address.trim().length > 0;
  const isSummaryValid = state.form.summary && state.form.summary.trim().length > 0;
  const hasAtLeastOneSkill = state.skills && state.skills.length > 0;

  if (!isPhoneValid) missingFields.push('phone');
  if (!isAddressValid) missingFields.push('address');
  if (!isSummaryValid) missingFields.push('summary');
  if (!hasAtLeastOneSkill) missingFields.push('skills');

  if (missingFields.length > 0) {
    return {
      error: 'Phone, address, bio, and at least one skill are required before saving',
      missingFields,
    };
  }

  if (state.form.phone) {
    const rawPhone = state.form.phone.trim();
    const phoneForFormatValidation = rawPhone.startsWith('+') ? rawPhone : `+${rawPhone}`;
    if (!isValidPhoneNumber(phoneForFormatValidation)) {
      return {
        error: "Phone number invalid. Format: +{Code} {Number} (e.g. +1 5551234567)",
        missingFields: [],
      };
    }
  }

  const invalidEducation = state.education.some((item) => !item.universityName.trim() || !item.universityId);
  if (invalidEducation) {
    return {
      error: 'Education entries must include a selected university.',
      missingFields: [],
    };
  }

  const invalidSkills = state.skills.some((item) => !item.name.trim() || !item.skillId);
  if (invalidSkills) {
    return {
      error: 'Skills entries must include a selected skill.',
      missingFields: [],
    };
  }

  const invalidLanguages = state.languages.some((item) => !item.name.trim() || !item.languageId);
  if (invalidLanguages) {
    return {
      error: 'Language entries must include a selected language.',
      missingFields: [],
    };
  }

  const invalidExperience = state.experience.some((item) => !item.position.trim() || !item.positionId);
  if (invalidExperience) {
    return {
      error: 'Experience entries must include a selected position.',
      missingFields: [],
    };
  }

  const invalidProjects = state.projects.some((item) => !item.name.trim());
  if (invalidProjects) {
    return {
      error: 'Project name is required for every project entry.',
      missingFields: [],
    };
  }

  const invalidCertificates = state.certificates.some((item) => !item.name.trim());
  if (invalidCertificates) {
    return {
      error: 'Certificate name is required for every certificate entry.',
      missingFields: [],
    };
  }

  return { error: null, missingFields: [] };
};
