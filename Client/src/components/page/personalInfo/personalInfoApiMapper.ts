import type { UpdatePersonalInfoDto } from '../../../types/dto';
import type { PersonalInfoState } from './personalInfoStateHelpers';

const sanitizePhoneForApi = (phone?: string | null) => {
  if (!phone) {
    return undefined;
  }

  let normalized = phone.trim();
  if (normalized.startsWith('+')) {
    normalized = normalized.slice(1);
  }

  normalized = normalized.replace(/[^0-9 ]+/g, '').trim();
  if (!normalized) {
    return undefined;
  }

  const parts = normalized.split(/\s+/);
  const countryCode = parts[0] || '';
  const number = parts.slice(1).join(' ');
  return countryCode ? `+${countryCode}${number ? ` ${number}` : ''}` : `+${normalized}`;
};

/**
 * Converts Personal Info page state into the backend update DTO.
 */
export const mapPersonalInfoStateToUpdateDto = (state: PersonalInfoState): UpdatePersonalInfoDto => ({
  ...state.form,
  phone: sanitizePhoneForApi(state.form.phone),
  education: state.education,
  skills: state.skills,
  languages: state.languages,
  experience: state.experience,
  projects: state.projects,
  certificates: state.certificates,
});
