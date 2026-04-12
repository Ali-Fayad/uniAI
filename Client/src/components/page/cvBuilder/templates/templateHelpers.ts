import type { CVSectionKey, PersonalInfoResponseDto, SelectedItemsDto } from '../../../../types/dto';

export interface TemplateSectionData {
  key: CVSectionKey;
  title: string;
  items: string[];
}

const SECTION_LABELS: Record<CVSectionKey, string> = {
  education: 'Education',
  experience: 'Experience',
  skills: 'Skills',
  languages: 'Languages',
  projects: 'Projects',
  certificates: 'Certificates',
};

export const getDisplayName = () => 'Your Name';

export const getContactItems = (personalInfo: PersonalInfoResponseDto | null): string[] => {
  if (!personalInfo) {
    return [];
  }

  return [
    personalInfo.phone,
    personalInfo.address,
    personalInfo.linkedin,
    personalInfo.github,
    personalInfo.portfolio,
  ].filter(Boolean) as string[];
};

export const getSectionData = (
  personalInfo: PersonalInfoResponseDto | null,
  order: CVSectionKey[],
  selectedItems?: SelectedItemsDto
): TemplateSectionData[] => {
  const map: Record<CVSectionKey, string[]> = {
    education:
      personalInfo?.education?.filter(e => !selectedItems || !selectedItems.educationIds || selectedItems.educationIds.includes(e.id)).map((entry) =>
        entry.universityName ? entry.universityName : 'Education entry',
      ) ?? [],
    experience:
      personalInfo?.experience?.filter(e => !selectedItems || !selectedItems.experienceIds || selectedItems.experienceIds.includes(e.id)).map((entry) => `${entry.position}${entry.company ? ` at ${entry.company}` : ''}`) ?? [],
    skills: personalInfo?.skills?.filter(e => !selectedItems || !selectedItems.skillIds || selectedItems.skillIds.includes(e.id)).map((entry) => entry.name) ?? [],
    languages: personalInfo?.languages?.filter(e => !selectedItems || !selectedItems.languageIds || selectedItems.languageIds.includes(e.id)).map((entry) => entry.name) ?? [],
    projects:
      personalInfo?.projects?.filter(e => !selectedItems || !selectedItems.projectIds || selectedItems.projectIds.includes(e.id)).map((entry) => `${entry.name}${entry.description ? ` — ${entry.description}` : ''}`) ?? [],
    certificates:
      personalInfo?.certificates?.filter(e => !selectedItems || !selectedItems.certificateIds || selectedItems.certificateIds.includes(e.id)).map((entry) => `${entry.name}${entry.issuer ? ` — ${entry.issuer}` : ''}`) ?? [],
  };

  return order
    .map((key) => ({
      key,
      title: SECTION_LABELS[key],
      items: map[key],
    }))
    .filter((section) => section.items.length > 0);
};
