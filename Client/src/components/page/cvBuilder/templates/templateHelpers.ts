import type { CVSectionKey, PersonalInfoResponseDto, SelectedItemsDto, ItemsOrderDto } from '../../../../types/dto';

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
  selectedItems?: SelectedItemsDto,
  itemsOrder?: ItemsOrderDto
): TemplateSectionData[] => {
  const getOrderedItems = <T extends { id: string }>(items: T[] | undefined, orderIds: string[] | undefined, isSelected: (id: string) => boolean) => {
    if (!items) return [];
    const filtered = items.filter(e => isSelected(e.id));
    if (!orderIds || orderIds.length === 0) return filtered;
    
    return filtered.sort((a, b) => {
      const idxA = orderIds.indexOf(a.id);
      const idxB = orderIds.indexOf(b.id);
      if (idxA === -1 && idxB === -1) return 0;
      if (idxA === -1) return 1;
      if (idxB === -1) return -1;
      return idxA - idxB;
    });
  };

  const map: Record<CVSectionKey, string[]> = {
    education: getOrderedItems(personalInfo?.education, itemsOrder?.educationIds, id => !selectedItems || !selectedItems.educationIds || selectedItems.educationIds.includes(id))
      .map((entry) => entry.universityName ? entry.universityName : 'Education entry'),
    experience: getOrderedItems(personalInfo?.experience, itemsOrder?.experienceIds, id => !selectedItems || !selectedItems.experienceIds || selectedItems.experienceIds.includes(id))
      .map((entry) => `${entry.position}${entry.company ? ` at ${entry.company}` : ''}`),
    skills: getOrderedItems(personalInfo?.skills, itemsOrder?.skillIds, id => !selectedItems || !selectedItems.skillIds || selectedItems.skillIds.includes(id))
      .map((entry) => entry.name),
    languages: getOrderedItems(personalInfo?.languages, itemsOrder?.languageIds, id => !selectedItems || !selectedItems.languageIds || selectedItems.languageIds.includes(id))
      .map((entry) => entry.name),
    projects: getOrderedItems(personalInfo?.projects, itemsOrder?.projectIds, id => !selectedItems || !selectedItems.projectIds || selectedItems.projectIds.includes(id))
      .map((entry) => `${entry.name}${entry.description ? ` — ${entry.description}` : ''}`),
    certificates: getOrderedItems(personalInfo?.certificates, itemsOrder?.certificateIds, id => !selectedItems || !selectedItems.certificateIds || selectedItems.certificateIds.includes(id))
      .map((entry) => `${entry.name}${entry.issuer ? ` — ${entry.issuer}` : ''}`),
  };

  return order
    .map((key) => ({
      key,
      title: SECTION_LABELS[key],
      items: map[key],
    }))
    .filter((section) => section.items.length > 0);
};
