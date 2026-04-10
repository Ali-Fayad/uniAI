import type { CVSectionKey, PersonalInfoResponseDto } from '../../../../types/dto';

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
): TemplateSectionData[] => {
  const map: Record<CVSectionKey, string[]> = {
    education:
      personalInfo?.education?.map((entry) =>
        entry.universityName ? entry.universityName : 'Education entry',
      ) ?? [],
    experience:
      personalInfo?.experience?.map((entry) => `${entry.position}${entry.company ? ` at ${entry.company}` : ''}`) ?? [],
    skills: personalInfo?.skills?.map((entry) => entry.name) ?? [],
    languages: personalInfo?.languages?.map((entry) => entry.name) ?? [],
    projects:
      personalInfo?.projects?.map((entry) => `${entry.name}${entry.description ? ` — ${entry.description}` : ''}`) ?? [],
    certificates:
      personalInfo?.certificates?.map((entry) => `${entry.name}${entry.issuer ? ` — ${entry.issuer}` : ''}`) ?? [],
  };

  return order
    .map((key) => ({
      key,
      title: SECTION_LABELS[key],
      items: map[key],
    }))
    .filter((section) => section.items.length > 0);
};
