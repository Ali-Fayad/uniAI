import re

file_path = '/home/afayad2/uniAI/Client/src/components/page/personalInfo/personalInfoStateHelpers.ts'

with open(file_path, 'r') as f:
    content = f.read()

interface_code = """
export interface ValidationResult {
  error: string | null;
  missingFields: string[];
}
"""

insert_pos = content.find('export const createEmptyPersonalInfoFormState')
if insert_pos > 0:
    content = content[:insert_pos] + interface_code + '\n' + content[insert_pos:]

new_function = '''export const validatePersonalInfoState = (state: PersonalInfoState): ValidationResult => {
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

  if (state.form.phone && !isValidPhoneNumber(state.form.phone)) {
    return {
      error: "Phone number invalid. Must be in format '+TotalDigits RegionDigits' (e.g. +1 5551234567)",
      missingFields: [],
    };
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
};'''

# Replace validatePersonalInfoState
pattern = r'export const validatePersonalInfoState =.*?null;\n};'
content = re.sub(pattern, new_function, content, flags=re.DOTALL)

with open(file_path, 'w') as f:
    f.write(content)
